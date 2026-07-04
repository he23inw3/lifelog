package jp.he23inw3.asset.domain.constant;

/**
 * Firestore のコレクション名を定義する定数クラス。
 */
public final class FirestoreCollectionNames {

    /** ユーザー設定ドキュメントのコレクション名 */
    public static final String USER_SETTINGS = "user_settings";

    /** セッション状態管理ドキュメントのコレクション名 */
    public static final String USER_SESSIONS = "user_sessions";

    /** バッチ実行履歴ドキュメントのコレクション名 */
    public static final String BATCH_EXECUTION_HISTORY = "batch_execution_logs";

    /** 管理者ユーザー情報のコレクション名 */
    public static final String ADMIN_USERS = "admin_users";

    /** デモ用擬似カレンダーイベントのコレクション名 */
    public static final String DEMO_CALENDAR_EVENTS = "demo_calendar_events";

    /** デモ用 Slack メッセージのコレクション名 */
    public static final String DEMO_SLACK_MESSAGES = "demo_slack_messages";

    /** ユーザー認証情報ドキュメントのコレクション名 */
    public static final String USER_CREDENTIALS = "user_credentials";

    /** Slack連携トークン管理用コレクション名 */
    public static final String SLACK_LINKAGE_TOKENS = "slack_linkage_tokens";

    private FirestoreCollectionNames() {
        // インスタンス化禁止
    }
}
