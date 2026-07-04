package jp.he23inw3.asset.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.exception.InvalidTokenException;
import jp.he23inw3.asset.domain.model.SlackLinkageToken;
import jp.he23inw3.asset.domain.repository.SlackLinkageTokenRepository;
import jp.he23inw3.asset.domain.service.UserSettingService;
import jp.he23inw3.asset.domain.util.InstantUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Slack アカウントの連携および一時トークンの発行・検証フローを制御するユースケースクラス。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SlackLinkageUseCase {

    private final LifeLogConfig config;
    private final UserSettingService userSettingService;
    private final SlackLinkageTokenRepository linkageTokenRepository;

    /**
     * アカウント連携用の一時トークンを生成し、連携URLを返します。
     *
     * @param userId Slack ユーザーID
     * @return 連携URL
     */
    public String generateLinkageUrl(String userId) {
        String token = UUID.randomUUID().toString();
        Instant expiresAt = InstantUtil.now().plus(Duration.ofMinutes(10));

        SlackLinkageToken linkageToken = SlackLinkageToken.builder()
                .token(token)
                .slackUserId(userId)
                .expiresAt(expiresAt)
                .build();

        linkageTokenRepository.save(linkageToken);

        String portalUrl = config.portal().baseUrl();
        return portalUrl + "/settings?slackToken=" + token;
    }

    /**
     * 一時トークンを検証し、現在のログインユーザーに Slack ユーザーIDを紐付けます。
     *
     * @param email ログインユーザーのメールアドレス
     * @param token 連携用の一時トークン
     */
    public void linkSlack(String email, String token) {
        String slackUserId = verifyAndConsumeToken(token);
        userSettingService.linkSlackAccount(email, slackUserId);
    }

    /**
     * アカウント連携用の一時トークンを検証し、関連付けられた Slack ユーザーIDを返します。
     * トークンは消費（削除）されます。
     *
     * @param token 検証対象の一時トークン
     * @return Slack ユーザーID
     * @throws InvalidTokenException トークンが無効または期限切れの場合
     */
    public String verifyAndConsumeToken(String token) {
        SlackLinkageToken linkageToken = linkageTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired token."));

        if (linkageToken.isExpired()) {
            linkageTokenRepository.delete(token);
            throw new InvalidTokenException("Linkage token has expired.");
        }

        String slackUserId = linkageToken.getSlackUserId();
        if (StringUtils.isBlank(slackUserId)) {
            throw new InvalidTokenException("Invalid token data.");
        }

        // トークンを削除 (ワンタイム)
        linkageTokenRepository.delete(token);
        return slackUserId;
    }
}
