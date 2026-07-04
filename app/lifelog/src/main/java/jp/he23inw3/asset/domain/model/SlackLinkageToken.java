package jp.he23inw3.asset.domain.model;

import java.time.Instant;
import jp.he23inw3.asset.domain.util.InstantUtil;
import lombok.Builder;
import lombok.Value;

/**
 * Slack アカウント連携用の一時トークン情報を保持するドメインモデル（値オブジェクト）。
 */
@Value
@Builder(toBuilder = true)
public class SlackLinkageToken {

    /** 連携用の一時トークン (UUID) */
    String token;

    /** 対象の Slack ユーザーID */
    String slackUserId;

    /** 有効期限 */
    Instant expiresAt;

    /**
     * トークンが期限切れかどうかを判定します。
     *
     * @return 期限切れの場合は true
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(InstantUtil.now());
    }
}
