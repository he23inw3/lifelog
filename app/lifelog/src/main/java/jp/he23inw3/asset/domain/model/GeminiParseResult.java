package jp.he23inw3.asset.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * Gemini による自然言語パース結果を表すドメインモデル（値オブジェクト）。
 * <p>
 * 入力が日報関連であるかどうかのフラグや、抽出された稼働時間・タスク内容、 およびユーザーへ返却する応答メッセージのテンプレートを保持します。
 */
@Value
@Builder(toBuilder = true)
public class GeminiParseResult {

    /** 日報関連フラグ */
    boolean logRelated;

    /** ログ日付 */
    String logDate;

    /** 休暇フラグ */
    boolean holiday;

    /** タスク */
    String tasks;

    /** 稼働時間 */
    double workHours;

    /** 残業時間 */
    double overtimeHours;

    /** 日記 */
    String diary;

    /** 感情 */
    Sentiment sentiment;

    /** 応答メッセージ */
    String replyMessage;
}
