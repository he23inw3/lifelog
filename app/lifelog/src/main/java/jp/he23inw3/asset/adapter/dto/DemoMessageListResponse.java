package jp.he23inw3.asset.adapter.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * デモ用 Slack メッセージ一覧レスポンス DTO。
 */
@Data
@Builder
public class DemoMessageListResponse {

    /** メッセージ総数 */
    private int totalSize;

    /** メッセージ一覧 */
    private List<DemoMessage> messages;

    @Data
    @Builder
    public static class DemoMessage {
        /** Slack ユーザー ID */
        private String slackUserId;
        /** メッセージタイプ (POST / CONFIRM / UPDATE) */
        private String type;
        /** メッセージ本文 */
        private String text;
        /** 送信日時 */
        private LocalDateTime timestamp;
    }
}
