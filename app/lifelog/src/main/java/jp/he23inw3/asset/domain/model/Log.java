package jp.he23inw3.asset.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

/**
 * ユーザーの登録する1日分の「日報ログ」を表すドメインモデル（値オブジェクト）。
 * <p>
 * ユーザーの Slack ID、対象日付、未加工の入力テキスト、休暇フラグ、 パース済みのタスク一覧や稼働時間、および日記・感情データなどを内包します。
 */
@Value
@Builder(toBuilder = true)
public class Log {

    /** SlackユーザID */
    String slackUserId;

    /** ログ日付 */
    LocalDate logDate;

    /** 未加工テキスト */
    String rawText;

    /** 休暇フラグ */
    boolean holiday;

    /** タスク一覧 */
    String tasks;

    /** 稼働時間 */
    Double workHours;

    /** 残業時間 */
    Double overtimeHours;

    /** 日記 */
    String diary;

    /** 感情 */
    Sentiment sentiment;

    /** 作成日時 */
    Instant createdAt;

    /** 更新日時 */
    Instant updatedAt;
}
