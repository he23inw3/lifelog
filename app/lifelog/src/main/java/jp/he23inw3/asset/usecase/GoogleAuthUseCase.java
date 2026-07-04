package jp.he23inw3.asset.usecase;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.enterprise.context.ApplicationScoped;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.gateway.CryptoGateway;
import jp.he23inw3.asset.domain.service.UserSettingService;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Google OAuth 2.0 認可および連携フローを制御するユースケースクラス。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class GoogleAuthUseCase {

    private final LifeLogConfig config;
    private final CryptoGateway cryptoGateway;
    private final UserSettingService userSettingService;

    /**
     * Google OAuth ログイン画面へリダイレクトするためのURLを返します。 デモモードが有効な場合は、即時に連携処理を実行して完了URLを返します。
     *
     * @param email
     *            連携対象のユーザーメールアドレス
     * @return リダイレクト先URL
     */
    public String getLoginRedirectUrl(String email) {
        String portalUrl = config.portal().baseUrl();

        if (StringUtils.isBlank(email)) {
            log.warn(MessageHelper.getMessage("usecase.googleauth.login.noemail"));
            return portalUrl + "/settings?google=error&message=email_required";
        }

        // デモモードの場合は、OAuth をスキップして直接成功リダイレクトと擬似更新を行う
        if (config.demo().enabled()) {
            log.info(MessageHelper.getMessage("usecase.googleauth.demo.skip", email));
            try {
                userSettingService.linkGoogleAccount(email, "dummy-demo-refresh-token");
                log.info(MessageHelper.getMessage("usecase.googleauth.demo.success", email));
                return portalUrl + "/settings?google=success";
            } catch (ResourceNotFoundException e) {
                log.warn(MessageHelper.getMessage("usecase.googleauth.demo.usernotfound", email));
                return portalUrl + "/settings?google=error&message=user_not_found";
            } catch (Exception e) {
                log.error(MessageHelper.getMessage("usecase.googleauth.demo.error"), e);
                return portalUrl + "/settings?google=error";
            }
        }

        try {
            // 紐付け対象のメールアドレスを暗号化して state パラメータに設定
            String state = cryptoGateway.encrypt(email);

            String authorizationUrl = String.format(
                    "https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s&access_type=offline&prompt=consent",
                    URLEncoder.encode(config.google().oauthClientId(), StandardCharsets.UTF_8),
                    URLEncoder.encode(config.google().oauthRedirectUri(), StandardCharsets.UTF_8),
                    URLEncoder.encode("https://www.googleapis.com/auth/calendar openid email", StandardCharsets.UTF_8),
                    URLEncoder.encode(state, StandardCharsets.UTF_8));

            log.info(MessageHelper.getMessage("usecase.googleauth.redirect", email));
            return authorizationUrl;

        } catch (Exception e) {
            log.error(MessageHelper.getMessage("usecase.googleauth.url.error"), e);
            return portalUrl + "/settings?google=error";
        }
    }

    /**
     * Google OAuth からのコールバックパラメータを受け取り、トークンを検証・保存してリダイレクト先URLを返します。
     *
     * @param state OAuth 2.0 State
     * @param code Authorization Code
     * @param error Error
     * @return リダイレクト先URL
     */
    public String handleCallback(String state, String code, String error) {
        String portalUrl = config.portal().baseUrl();

        if (StringUtils.isNotBlank(error)) {
            log.warn(MessageHelper.getMessage("usecase.googleauth.callback.error", error));
            return portalUrl + "/settings?google=error&message=" + error;
        }

        if (StringUtils.isBlank(state) || StringUtils.isBlank(code)) {
            log.warn(MessageHelper.getMessage("usecase.googleauth.callback.missing"));
            return portalUrl + "/settings?google=error&message=missing_parameters";
        }

        String targetEmail;
        try {
            // state からメールアドレスを復号
            targetEmail = cryptoGateway.decrypt(state);
            if (StringUtils.isBlank(targetEmail)) {
                throw new IllegalArgumentException("Decrypted email is null or empty");
            }
        } catch (Exception e) {
            log.error(MessageHelper.getMessage("usecase.googleauth.callback.decrypt.error"), e);
            return portalUrl + "/settings?google=error&message=invalid_state";
        }

        try {
            // Token Exchange
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(), "https://oauth2.googleapis.com/token",
                    config.google().oauthClientId(), config.google().oauthClientSecret(), code,
                    config.google().oauthRedirectUri()).execute();

            String refreshToken = tokenResponse.getRefreshToken();
            GoogleIdToken idTokenObj = tokenResponse.parseIdToken();
            GoogleIdToken.Payload payload = idTokenObj.getPayload();
            String googleEmail = payload.getEmail();

            log.info(
                    MessageHelper.getMessage("usecase.googleauth.callback.exchange.success", googleEmail, targetEmail));

            // セキュリティチェック: 同意画面のGoogleアカウントとLifeLogのターゲットメールアドレスが一致するか検証
            if (StringUtils.isBlank(googleEmail) || !googleEmail.equalsIgnoreCase(targetEmail)) {
                log.warn(MessageHelper.getMessage("usecase.googleauth.callback.security.failed", googleEmail,
                        targetEmail));
                return portalUrl + "/settings?google=error&message=email_mismatch";
            }

            userSettingService.linkGoogleAccount(targetEmail, refreshToken);
            log.info(MessageHelper.getMessage("usecase.googleauth.callback.linked", targetEmail));
            return portalUrl + "/settings?google=success";
        } catch (ResourceNotFoundException e) {
            log.warn(MessageHelper.getMessage("usecase.googleauth.callback.usernotfound", targetEmail));
            return portalUrl + "/settings?google=error&message=user_not_found";
        } catch (IllegalArgumentException e) {
            log.warn(MessageHelper.getMessage("usecase.googleauth.callback.norefreshtoken", targetEmail));
            return portalUrl + "/settings?google=error&message=no_refresh_token";
        } catch (Exception e) {
            log.error(MessageHelper.getMessage("usecase.googleauth.callback.process.error"), e);
            return portalUrl + "/settings?google=error";
        }
    }
}
