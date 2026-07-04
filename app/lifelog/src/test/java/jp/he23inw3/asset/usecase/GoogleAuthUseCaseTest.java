package jp.he23inw3.asset.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.gateway.CryptoGateway;
import jp.he23inw3.asset.domain.service.UserSettingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleAuthUseCaseTest {

    @Mock
    LifeLogConfig config;

    @Mock
    LifeLogConfig.Demo configDemo;

    @Mock
    LifeLogConfig.Portal configPortal;

    @Mock
    LifeLogConfig.Google configGoogle;

    @Mock
    CryptoGateway cryptoGateway;

    @Mock
    UserSettingService userSettingService;

    @InjectMocks
    GoogleAuthUseCase target;

    @Nested
    @DisplayName("ログイン・連携用リダイレクトURL取得")
    class GetLoginRedirectUrl {

        @Test
        @DisplayName("メールアドレスが空の場合、エラーパラメータ付きのポータル設定画面URLを返すこと")
        void getLoginRedirectUrl_EmailEmpty() {
            when(config.portal()).thenReturn(configPortal);
            when(configPortal.baseUrl()).thenReturn("http://localhost:5173");

            String url = target.getLoginRedirectUrl(" ");

            assertThat(url).isEqualTo("http://localhost:5173/settings?google=error&message=email_required");
        }

        @Test
        @DisplayName("デモモード時、即座にGoogleアカウントを連携して成功URLを返すこと")
        void getLoginRedirectUrl_DemoMode_Success() {
            when(config.portal()).thenReturn(configPortal);
            when(configPortal.baseUrl()).thenReturn("http://localhost:5173");
            when(config.demo()).thenReturn(configDemo);
            when(configDemo.enabled()).thenReturn(true);

            String email = "demo@example.com";
            String url = target.getLoginRedirectUrl(email);

            assertThat(url).isEqualTo("http://localhost:5173/settings?google=success");
            verify(userSettingService).linkGoogleAccount(email, "dummy-demo-refresh-token");
        }

        @Test
        @DisplayName("デモモード時、ユーザー設定が見つからない場合はエラーURLを返すこと")
        void getLoginRedirectUrl_DemoMode_UserNotFound() {
            when(config.portal()).thenReturn(configPortal);
            when(configPortal.baseUrl()).thenReturn("http://localhost:5173");
            when(config.demo()).thenReturn(configDemo);
            when(configDemo.enabled()).thenReturn(true);

            String email = "missing@example.com";
            doThrow(new ResourceNotFoundException("Not found"))
                    .when(userSettingService).linkGoogleAccount(email, "dummy-demo-refresh-token");

            String url = target.getLoginRedirectUrl(email);

            assertThat(url).isEqualTo("http://localhost:5173/settings?google=error&message=user_not_found");
        }

        @Test
        @DisplayName("通常モード時、正しくGoogle同意画面のURLを構築して返すこと")
        void getLoginRedirectUrl_NormalMode_Success() {
            when(config.portal()).thenReturn(configPortal);
            when(configPortal.baseUrl()).thenReturn("http://localhost:5173");
            when(config.demo()).thenReturn(configDemo);
            when(configDemo.enabled()).thenReturn(false);
            when(config.google()).thenReturn(configGoogle);
            when(configGoogle.oauthClientId()).thenReturn("client-id");
            when(configGoogle.oauthRedirectUri()).thenReturn("http://localhost:5000/api/v1/auth/google/callback");
            when(cryptoGateway.encrypt("user@example.com")).thenReturn("encrypted-state");

            String url = target.getLoginRedirectUrl("user@example.com");

            assertThat(url).contains("accounts.google.com");
            assertThat(url).contains("client_id=client-id");
            assertThat(url).contains("state=encrypted-state");
        }

        @Test
        @DisplayName("通常モード時、暗号化失敗時はエラーURLを返すこと")
        void getLoginRedirectUrl_NormalMode_EncryptionError() {
            when(config.portal()).thenReturn(configPortal);
            when(configPortal.baseUrl()).thenReturn("http://localhost:5173");
            when(config.demo()).thenReturn(configDemo);
            when(configDemo.enabled()).thenReturn(false);
            when(cryptoGateway.encrypt("user@example.com")).thenThrow(new RuntimeException("Encryption fail"));

            String url = target.getLoginRedirectUrl("user@example.com");

            assertThat(url).isEqualTo("http://localhost:5173/settings?google=error");
        }
    }

    @Nested
    @DisplayName("OAuthコールバック処理")
    class HandleCallback {

        @Test
        @DisplayName("コールバック処理にて、Googleからエラーを受信した場合にエラーURLを返すこと")
        void handleCallback_GoogleError() {
            when(config.portal()).thenReturn(configPortal);
            when(configPortal.baseUrl()).thenReturn("http://localhost:5173");

            String url = target.handleCallback("state", "code", "access_denied");

            assertThat(url).isEqualTo("http://localhost:5173/settings?google=error&message=access_denied");
        }

        @Test
        @DisplayName("コールバック処理にて、パラメータが不足している場合にエラーURLを返すこと")
        void handleCallback_MissingParams() {
            when(config.portal()).thenReturn(configPortal);
            when(configPortal.baseUrl()).thenReturn("http://localhost:5173");

            String url = target.handleCallback(null, "code", null);

            assertThat(url).isEqualTo("http://localhost:5173/settings?google=error&message=missing_parameters");
        }

        @Test
        @DisplayName("コールバック処理にて、stateの復号に失敗した場合にエラーURLを返すこと")
        void handleCallback_InvalidState() {
            when(config.portal()).thenReturn(configPortal);
            when(configPortal.baseUrl()).thenReturn("http://localhost:5173");
            when(cryptoGateway.decrypt("invalid-state")).thenThrow(new RuntimeException("Decryption error"));

            String url = target.handleCallback("invalid-state", "code", null);

            assertThat(url).isEqualTo("http://localhost:5173/settings?google=error&message=invalid_state");
        }
    }
}
