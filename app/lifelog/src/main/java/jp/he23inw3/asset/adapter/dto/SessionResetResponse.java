package jp.he23inw3.asset.adapter.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 対話セッションリセット API (DELETE /api/v1/users/{slackUserId}/session) レスポンス DTO。
 */
@Data
@Builder
public class SessionResetResponse {

    /** リセット対象の Slack ユーザー ID */
    private String slackUserId;

    /** 処理結果メッセージ */
    private String message;
}
