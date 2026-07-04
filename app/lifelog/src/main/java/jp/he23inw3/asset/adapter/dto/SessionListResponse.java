package jp.he23inw3.asset.adapter.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * 進行中のセッション一覧レスポンス DTO。
 */
@Data
@Builder
public class SessionListResponse {

    /** セッション総数 */
    private int totalSize;

    /** セッション一覧 */
    private List<SessionResponse> sessions;

    /** セッション情報 */
    @Data
    @Builder(toBuilder = true)
    public static class SessionResponse {
        /** Slack ユーザー ID */
        private String slackUserId;

        /** セッションステータス */
        private String status;

        /** 更新日時 */
        private LocalDateTime updatedAt;

        /** 一時保存データ */
        private Map<String, String> tempData;

        /** 有効期限 */
        private LocalDateTime expiresAt;
    }
}
