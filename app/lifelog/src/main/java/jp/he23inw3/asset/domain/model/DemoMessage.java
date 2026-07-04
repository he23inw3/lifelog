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
    String slackUserId;

    /** メッセージタイプ */
    String type;

    /** メッセージ本文 */
    String text;

    /** メッセージタイムスタンプ */
    LocalDateTime timestamp;
}
