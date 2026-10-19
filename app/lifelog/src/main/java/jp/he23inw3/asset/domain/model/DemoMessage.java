package jp.he23inw3.asset.domain.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

/**
 * デモ用の Slack メッセージを表すドメインモデル。
 */
@Value
@Builder(toBuilder = true)
public class DemoMessage {

    /** Slack ユーザーID */
    private String slackUserId;

    /** メッセージタイプ */
    private String type;

    /** メッセージ本文 */
    private String text;

    /** メッセージタイムスタンプ */
    private LocalDateTime timestamp;
}
