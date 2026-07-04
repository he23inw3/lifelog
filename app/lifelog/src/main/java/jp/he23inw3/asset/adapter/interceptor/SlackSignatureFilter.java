package jp.he23inw3.asset.adapter.interceptor;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jp.he23inw3.asset.adapter.constant.ApiPath;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.util.InstantUtil;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Slack からの Webhook リクエストの署名検証を行うサーブレットフィルター。
 * <p>
 * {@code /api/slack/events} 宛てのリクエストを捕捉し、 ヘッダーに含まれるタイムスタンプと署名を検証することで、リクエストが本当に Slack から送信されたものかを検証します。
 */
@Provider
@RequiredArgsConstructor
@Slf4j
public class SlackSignatureFilter implements ContainerRequestFilter {

    private final jakarta.inject.Provider<LifeLogConfig> configProvider;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getRequestUri().getPath();
        if (!path.contains(ApiPath.SLACK_EVENTS) && !path.contains(ApiPath.SLACK_COMMANDS)) {
            return;
        }

        String timestamp = requestContext.getHeaderString("X-Slack-Request-Timestamp");
        String signature = requestContext.getHeaderString("X-Slack-Signature");

        if (StringUtils.isBlank(timestamp) || StringUtils.isBlank(signature)) {
            log.warn(MessageHelper.getMessage("adapter.slack.signature.missing"));
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        // リプレイアタック防止：タイムスタンプの検証（現在時刻から5分以内）
        try {
            long now = InstantUtil.nowEpochSecond();
            long requestTime = Long.parseLong(timestamp);
            if (Math.abs(now - requestTime) > 60 * 5) {
                log.warn(MessageHelper.getMessage("adapter.slack.signature.invalid") + " (Expired timestamp)");
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                return;
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid Slack timestamp format: " + timestamp);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        // リクエストボディの読み込み
        byte[] bodyBytes = requestContext.getEntityStream().readAllBytes();
        requestContext.setEntityStream(new ByteArrayInputStream(bodyBytes));

        String body = new String(bodyBytes, StandardCharsets.UTF_8);
        String secret = configProvider.get().slack().signingSecret();

        String baseString = "v0:" + timestamp + ":" + body;
        try {
            String expectedSignature = "v0=" + hmacSha256(secret, baseString);
            // タイミングアタック防止：定数時間でのバイト比較
            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8))) {
                log.warn(MessageHelper.getMessage("adapter.slack.signature.invalid"));
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            }
        } catch (Exception e) {
            log.error(MessageHelper.getMessage("adapter.slack.signature.error"), e);
            requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
        }
    }

    /**
     * 指定されたキーとデータを用いて HMAC-SHA256 署名を生成します。
     *
     * @param key 署名用秘密鍵 (Slack Signing Secret)
     * @param data 署名対象データ (タイムスタンプとリクエストボディを結合した文字列)
     * @return 16進数文字列で表現されたハッシュ署名値
     * @throws NoSuchAlgorithmException アルゴリズムが存在しない場合
     * @throws InvalidKeyException 秘密鍵が無効な場合
     */
    private String hmacSha256(String key, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256HMAC.init(secretKey);
        byte[] hash = sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
