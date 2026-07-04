package jp.he23inw3.asset.domain.constant;

/**
 * ユーザー向けに表示または返却する応答メッセージ・エラーメッセージを管理する定数クラス。
 */
public final class UserMessageConstants {

    /** Gemini API の JSON パースエラー */
    public static final String GEMINI_JSON_PARSE_API_ERROR = "GeminiのJSONパースでエラーが発生しました。";

    /** Gemini API の解析汎用エラー */
    public static final String GEMINI_PARSE_API_ERROR = "Geminiの解析処理でエラーが発生しました。";

    /** Gemini API の月間振り返りエラー */
    public static final String GEMINI_REFLECT_API_ERROR = "Geminiの振り返り処理でエラーが発生しました。";

    /** セッションリセット完了メッセージ */
    public static final String SESSION_RESET_MESSAGE = "セッションがリセットされました。";

    /** API 認証エラー */
    public static final String AUTH_UNAUTHORIZED = "認証エラーが発生しました。";

    /** API 認可エラー */
    public static final String AUTH_FORBIDDEN = "認可エラーが発生しました。アクセス権限がありません。";

    /** 管理者情報未登録エラー（{0} に管理者のメールアドレスが入ります） */
    public static final String ADMIN_NOT_FOUND = "管理者が見つかりません: {0}";

    /** ユーザー設定未登録エラー（{0} に Slack ユーザーID が入ります） */
    public static final String WORKFLOW_USER_NOT_FOUND = "ユーザー設定が見つかりません。先に設定を登録してください: {0}";

    /** 日報入力催促リマインドメッセージ */
    public static final String REMIND_MESSAGE = "本日の日報が未入力です。登録をお願いします。";

    /** 稼働時間入力不足時のデフォルト聞き返しメッセージ */
    public static final String WORKFLOW_ASK_HOURS = "勤務時間を教えてください。";

    /** 休日カレンダー登録時のデフォルトイベントタイトル */
    public static final String WORKFLOW_CALENDAR_TITLE_HOLIDAY = "[日報] 休日";

    /** 平日カレンダー登録時のデフォルトイベントタイトル（{0} に勤務時間が入ります） */
    public static final String WORKFLOW_CALENDAR_TITLE_WORK = "[日報] 勤務: {0}h";

    /** 日報登録正常完了時の応答メッセージ */
    public static final String WORKFLOW_COMPLETE_REPLY = "日報の登録が完了しました。";

    private UserMessageConstants() {
        // インスタンス化禁止
    }
}
