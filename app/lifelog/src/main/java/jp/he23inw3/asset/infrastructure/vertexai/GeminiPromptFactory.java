package jp.he23inw3.asset.infrastructure.vertexai;

import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Gemini に送信するプロンプトおよびレスポンススキーマを構築するファクトリクラス。
 */
@ApplicationScoped
public class GeminiPromptFactory {

    /**
     * ライフログ解析用のプロンプトを構築します。
     * <p>
     * 【プロンプト設計（日本語訳）】 ユーザー入力を解析して構造化されたJSONデータを抽出するアシスタントとしての指示です。
     * <ol>
     * <li>入力が日報や日記の記録に関連しているかどうかを判定し、'log_related'（true/false）に設定する。</li>
     * <li>対象日 'log_date' (YYYY-MM-DD) を特定する。言及がない場合は今日のデフォルト日付とする。</li>
     * <li>入力およびContext（暦ステータス）に基づき、対象日が休日であるか判定して 'holiday'（true/false）に設定する。</li>
     * <li>'tasks' (作業実績) を抽出する。休日や不明の場合は空文字とする。</li>
     * <li>'work_hours' (総稼働時間) を抽出する。休日の場合は 0.0、平日かつ詳細不明・不足の場合は -1.0 とする。</li>
     * <li>'overtime_hours' (残業時間) を抽出する。残業なしや休日の場合は 0.0 とする。</li>
     * <li>'diary' (プライベートな日記) を抽出する。デフォルトは空文字。</li>
     * <li>'sentiment' (感情/メンタル状態) を分類する。値は Happy, Tired, Neutral, Stressed,
     * Relaxed, Bad。デフォルトは Neutral。</li>
     * <li>'reply_message' (ユーザーへの返信文)
     * を生成する。完了時は感謝・労い、平日かつ作業内容や稼働時間が不足している場合は優しく聞き返す。回答は日本語で行う。</li>
     * </ol>
     *
     * @param rawText
     *            ユーザーの入力テキスト
     * @param now
     *            基準日時
     * @param dayStatus
     *            暦ステータス（平日/土日祝/有給休暇）
     * @return 構築されたプロンプト文字列（英語）
     */
    public String createParsePrompt(String rawText, LocalDateTime now, String dayStatus) {
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return "You are an assistant that parses daily work logs and diaries into structured JSON.\n"
                + "User Input:\n"
                + "\"\"\"\n" + rawText + "\n\"\"\"\n\n" + "Context:\n" + "- Today's Date: "
                + formattedDate + "\n"
                + "- Today's Day Status: " + dayStatus + "\n\n" + "Instructions:\n"
                + "1. Determine if the input is related to recording a daily log (tasks, work hours, diary, sentiment, etc.). Set 'log_related' to true or false.\n"
                + "2. Identify target date 'log_date' (YYYY-MM-DD). If the user does not explicitly specify the date (e.g. today, yesterday, Sunday, or a specific date like June 21) in the input text, you MUST set 'log_date' to Today's Date from the Context. Do not leave it empty.\n"
                + "3. Identify if it is a holiday. Based on input and Context, set 'holiday' to true or false. Note that if Context is '祝日/休暇' (Holiday) but the user mentions work done, work hours, or work-related complaints (e.g. 'もうやめたい' or work stress), you MUST treat it as a potential workday (set 'holiday' to false) to validate work details, UNLESS the user explicitly states they did not work.\n"
                + "4. Extract 'tasks' (work done). If holiday or unknown/not worked, leave it empty.\n"
                + "5. Extract 'work_hours' (double). If holiday, set to 0.0. If weekday (including potential workday holiday) and unknown/insufficient details, set to -1.0.\n"
                + "6. Extract 'overtime_hours' (double). If none or holiday, set to 0.0.\n"
                + "7. Extract 'diary' (personal notes). Default to empty.\n"
                + "8. Classify 'sentiment' (Happy, Tired, Neutral, Stressed, Relaxed, Bad). Default to 'Neutral'.\n"
                + "9. Generate 'reply_message'. If all required details (work_hours/tasks on weekdays/potential workdays) are present, say thank you and state that the log has been registered. If it is a weekday/potential workday and 'work_hours' (value -1.0) or 'tasks' are missing, you MUST ask the user kindly for the missing information and DO NOT state that the log is registered or recorded. Never say 'registered' or 'recorded' if required information is missing. (Note: do NOT ask for the target date even if it is not explicitly mentioned, as it defaults to Today's Date). Answer in Japanese.";
    }

    /**
     * 月末振り返り用のプロンプトを構築します。
     * <p>
     * 【プロンプト設計（日本語訳）】 1ヶ月分の日報・日記サマリーを元に、日本語でパーソナルな月末振り返りレポートを作成する指示です。 <br>
     * レポートの構成案：
     * <ol>
     * <li>今月の総括（稼働時間や残業時間の分析、がんばったことへの労い）</li>
     * <li>業務・プライベートのトピックス（頻出ワードや主要な出来事の整理）</li>
     * <li>メンタル・感情の傾向分析（感情の起伏や傾向の可視化）</li>
     * <li>来月に向けた温かいアドバイスやエール</li>
     * </ol>
     *
     * @param summaryText
     *            月間サマリーテキスト
     * @return 構築されたプロンプト文字列（英語）
     */
    public String createReflectionPrompt(String summaryText) {
        return "Please generate a personal monthly reflection report for the user in Japanese based on the following monthly summary of work logs and diaries.\n\n"
                + "Proposed report structure:\n"
                + "1. Monthly Summary (Work hours, overtime hours, and appreciation for hard work)\n"
                + "2. Professional and Private Topics (Summary of frequently appearing words and key events)\n"
                + "3. Mental and Emotional Trend Analysis\n"
                + "4. Warm advice and encouragement for the upcoming month\n\n" + "Input Data:\n"
                + summaryText;
    }

    /**
     * ライフログ解析用のレスポンス JSON スキーマを構築します。
     * <p>
     * Structured Outputs に必要な JSON スキーマ定義を構築します。 各プロパティ（log_related, log_date,
     * holiday, tasks, work_hours, overtime_hours, diary, sentiment, reply_message）
     * の型や説明（description）を含みます。
     *
     * @return レスポンススキーマ
     */
    public Schema createParseResponseSchema() {
        // @formatter:off
        return Schema.builder().type(Type.Known.OBJECT).properties(Map.of(
                "log_related", Schema.builder()
                        .type(Type.Known.BOOLEAN)
                        .description("入力が日報・日記の記録に関連している場合 true。関係ない雑談等は false。")
                        .build(),
                "log_date", Schema.builder()
                        .type(Type.Known.STRING)
                        .description("対象日（YYYY-MM-DD）。ユーザーのテキストに日付の指定（今日、昨日、具体的な日付など）がない場合は、ContextのToday's Date（今日の日付）にしてください。")
                        .build(),
                "holiday", Schema.builder()
                        .type(Type.Known.BOOLEAN)
                        .description("対象日が土日祝または有休休暇で、かつ仕事をしていない場合 true。稼働日、または土日祝でも仕事をした（あるいはその可能性がある）場合は false。")
                        .build(),
                "tasks", Schema.builder()
                        .type(Type.Known.STRING)
                        .description("抽出された業務内容・作業実績。休日・不明の場合は空文字。")
                        .build(),
                "work_hours", Schema.builder()
                        .type(Type.Known.NUMBER)
                        .description("抽出された総稼働時間（単位: 時間）。休日は 0.0。平日または休日出勤の可能性がある日で不明・不足時は -1.0。")
                        .build(),
                "overtime_hours", Schema.builder()
                        .type(Type.Known.NUMBER)
                        .description("抽出された残業時間。不明・なし・休日の場合は 0.0。")
                        .build(),
                "diary", Schema.builder()
                        .type(Type.Known.STRING)
                        .description("抽出されたプライベートな出来事や日記。").build(),
                "sentiment",
                Schema.builder().type(Type.Known.STRING)
                        .description("その日の入力から読み取れるメンタル状態（Happy, Tired, Neutral, Stressed等）。")
                        .build(),
                "reply_message",
                Schema.builder().type(Type.Known.STRING)
                        .description("ユーザーに返す文章。正常終了時は感謝・労い、不足時は優しい聞き返し文章。").build()))
                .required(List.of("log_related", "log_date", "holiday", "tasks", "work_hours",
                        "overtime_hours",
                        "diary", "sentiment", "reply_message"))
                .build();
        // @formatter:on
    }
}
