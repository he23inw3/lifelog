package jp.he23inw3.asset.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Optional;
import jp.he23inw3.asset.domain.exception.AlreadyExistsException;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.gateway.CryptoGateway;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserSettingServiceTest {

    @Mock
    UserSettingRepository settingRepository;
    @Mock
    CryptoGateway cryptoGateway;

    @InjectMocks
    UserSettingService target;

    @Nested
    @DisplayName("Slackアカウントの連携処理")
    class LinkSlackAccount {

        @Test
        @DisplayName("すでに同じSlack IDが紐付いている場合、そのままsaveして返却すること")
        void linkSlackAccount_ExistingEmail_MatchingSlackId() {
            String email = "user@example.com";
            String slackUserId = "U12345";
            UserSetting existing = UserSetting.builder().email(email).slackUserId(slackUserId).userName("user").build();

            when(settingRepository.findByEmail(email)).thenReturn(Optional.of(existing));

            UserSetting result = target.linkSlackAccount(email, slackUserId);

            assertThat(result).isEqualTo(existing);
            verify(settingRepository).save(existing);
        }

        @Test
        @DisplayName("別のSlack IDへの移行を検出した場合、古いIDでの登録を移行してsaveWithMigrationを実行すること")
        void linkSlackAccount_MigrationRequired() {
            String email = "user@example.com";
            String newSlackUserId = "U99999";
            UserSetting existing = UserSetting.builder().email(email).slackUserId("U12345").userName("user").build();

            when(settingRepository.findByEmail(email)).thenReturn(Optional.of(existing));

            UserSetting result = target.linkSlackAccount(email, newSlackUserId);

            assertThat(result.getSlackUserId()).isEqualTo(newSlackUserId);
            verify(settingRepository).saveWithMigration(eq("U12345"), any(UserSetting.class));
        }

        @Test
        @DisplayName("メールアドレスで設定がなく、Slack IDですでに紐付け済みの場合はメールアドレスを更新して保存すること")
        void linkSlackAccount_ExistingSlackId_UpdateEmail() {
            String email = "user@example.com";
            String slackUserId = "U12345";
            UserSetting existing = UserSetting.builder().email("old@example.com").slackUserId(slackUserId).userName("user").build();

            when(settingRepository.findByEmail(email)).thenReturn(Optional.empty());
            when(settingRepository.findById(slackUserId)).thenReturn(Optional.of(existing));

            UserSetting result = target.linkSlackAccount(email, slackUserId);

            assertThat(result.getEmail()).isEqualTo(email);
            verify(settingRepository).save(any(UserSetting.class));
        }

        @Test
        @DisplayName("完全新規のアカウント連携の場合、デフォルト値で新規作成されて保存されること")
        void linkSlackAccount_NewAccount() {
            String email = "newuser@example.com";
            String slackUserId = "U00000";

            when(settingRepository.findByEmail(email)).thenReturn(Optional.empty());
            when(settingRepository.findById(slackUserId)).thenReturn(Optional.empty());

            UserSetting result = target.linkSlackAccount(email, slackUserId);

            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getSlackUserId()).isEqualTo(slackUserId);
            assertThat(result.getUserName()).isEqualTo("newuser");
            assertThat(result.getRemindTime()).isEqualTo("18:00");
            verify(settingRepository).save(any(UserSetting.class));
        }
    }

    @Nested
    @DisplayName("Googleアカウント連携")
    class LinkGoogleAccount {

        @Test
        @DisplayName("Google連携時、リフレッシュトークンを暗号化して正常にsaveできること")
        void linkGoogleAccount_Success() {
            String email = "user@example.com";
            String refreshToken = "raw-refresh-token";
            UserSetting existing = UserSetting.builder().email(email).googleLinked(false).build();

            when(settingRepository.findByEmail(email)).thenReturn(Optional.of(existing));
            when(cryptoGateway.encrypt(refreshToken)).thenReturn("encrypted-refresh-token");

            target.linkGoogleAccount(email, refreshToken);

            ArgumentCaptor<UserSetting> captor = ArgumentCaptor.forClass(UserSetting.class);
            verify(settingRepository).save(captor.capture());
            UserSetting saved = captor.getValue();
            assertThat(saved.isGoogleLinked()).isTrue();
            assertThat(saved.getGoogleRefreshToken()).isEqualTo("encrypted-refresh-token");
        }

        @Test
        @DisplayName("Google連携時、リフレッシュトークンが空でも既存のリフレッシュトークンがあれば成功すること")
        void linkGoogleAccount_Success_WithExistingRefreshToken() {
            String email = "user@example.com";
            UserSetting existing = UserSetting.builder().email(email).googleLinked(false)
                    .googleRefreshToken("existing-token").build();

            when(settingRepository.findByEmail(email)).thenReturn(Optional.of(existing));

            target.linkGoogleAccount(email, null);

            ArgumentCaptor<UserSetting> captor = ArgumentCaptor.forClass(UserSetting.class);
            verify(settingRepository).save(captor.capture());
            UserSetting saved = captor.getValue();
            assertThat(saved.isGoogleLinked()).isTrue();
            assertThat(saved.getGoogleRefreshToken()).isEqualTo("existing-token");
        }

        @Test
        @DisplayName("Google連携時、リフレッシュトークンが空で、かつ既存のものもない場合は例外がスローされること")
        void linkGoogleAccount_Failure_MissingRefreshToken() {
            String email = "user@example.com";
            UserSetting existing = UserSetting.builder().email(email).googleLinked(false).googleRefreshToken(null).build();

            when(settingRepository.findByEmail(email)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> target.linkGoogleAccount(email, "")).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Google refresh token is missing");
        }

        @Test
        @DisplayName("Google連携時、ユーザーの設定が存在しない場合は例外がスローされること")
        void linkGoogleAccount_Failure_UserNotFound() {
            String email = "missing@example.com";

            when(settingRepository.findByEmail(email)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> target.linkGoogleAccount(email, "token")).isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User settings not found");
        }
    }

    @Nested
    @DisplayName("利用者の初回登録")
    class RegisterUser {

        @Test
        @DisplayName("メールアドレスとSlack IDが重複していない場合、登録が成功しsaveされること")
        void registerUser_Success() {
            UserSetting setting = UserSetting.builder()
                    .email("new@example.com")
                    .slackUserId("U12345")
                    .build();

            when(settingRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
            when(settingRepository.findById("U12345")).thenReturn(Optional.empty());

            UserSetting result = target.registerUser(setting);

            assertThat(result).isEqualTo(setting);
            verify(settingRepository).save(setting);
        }

        @Test
        @DisplayName("メールアドレスが既に存在する場合、AlreadyExistsExceptionを投げること")
        void registerUser_DuplicateEmail_ThrowsException() {
            UserSetting setting = UserSetting.builder()
                    .email("existing@example.com")
                    .slackUserId("U12345")
                    .build();

            when(settingRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(setting));

            assertThatThrownBy(() -> target.registerUser(setting))
                    .isInstanceOf(AlreadyExistsException.class)
                    .hasMessage("このメールアドレスは既に登録されています。");
            verify(settingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Slack IDが既に存在する場合、AlreadyExistsExceptionを投げること")
        void registerUser_DuplicateSlackUserId_ThrowsException() {
            UserSetting setting = UserSetting.builder()
                    .email("new@example.com")
                    .slackUserId("existingSlack")
                    .build();

            when(settingRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
            when(settingRepository.findById("existingSlack")).thenReturn(Optional.of(setting));

            assertThatThrownBy(() -> target.registerUser(setting))
                    .isInstanceOf(AlreadyExistsException.class)
                    .hasMessage("この Slack ユーザー ID は既に登録されています。");
            verify(settingRepository, never()).save(any());
        }
    }
}
