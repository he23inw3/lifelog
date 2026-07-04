package jp.he23inw3.asset.domain.model;

import java.time.Instant;
import java.util.Map;
import jp.he23inw3.asset.domain.util.InstantUtil;
import lombok.Builder;
import lombok.Value;

/**
 * ユーザーとの対話フロー（ヒアリングセッション）の状態を保持するドメインモデル（値オブジェクト）。
 * <p>
 * セッションの進行状況を示すステータスや、一時的なパース結果（不足している稼働時間やタスク情報など）を保持します。
 */
@Value
@Builder(toBuilder = true)
public class Session {

    /** Slack ユーザID */
    String slackUserId;

    /** セッションステータス */
    SessionStatus status;

    /** 更新日時 */
    Instant updatedAt;

    /** 一時保存データ */
    Map<String, String> tempData;

    /** 有効期限 */
    Instant expiresAt;

    /**
     * セッションが期限切れかどうかを判定します。
     *
     * @return 期限切れの場合は true
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(InstantUtil.now());
    }
}
