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
    private String slackUserId;

    /** ログ日付 */
    private LocalDate logDate;

    /** 未加工テキスト */
    private String rawText;

    /** 休暇フラグ */
    private boolean holiday;

    /** タスク一覧 */
    private String tasks;

    /** 稼働時間 */
    private Double workHours;

    /** 残業時間 */
    private Double overtimeHours;

    /** 日記 */
    private String diary;

    /** 感情 */
    private Sentiment sentiment;

    /** 作成日時 */
    private Instant createdAt;

    /** 更新日時 */
    private Instant updatedAt;
}
