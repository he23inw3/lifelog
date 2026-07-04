package jp.he23inw3.asset.adapter.constant;

/**
 * API エンドポイントパス定数。
 */
public final class ApiPath {

    /** Slack Events API 受信エンドポイント */
    public static final String SLACK_EVENTS = "/api/slack/events";

    /** Slack Command API 受信エンドポイント */
    public static final String SLACK_COMMANDS = "/api/slack/commands";

    /** ユーザー向け API のベースパス */
    public static final String USERS_BASE = "/api/v1/users";

    /** ログインユーザー自身の操作 API のベースパス */
    public static final String USERS_ME = USERS_BASE + "/me";

    /** 管理 API のベースパス */
    public static final String ADMIN_BASE = "/api/v1/admin";

    /** 管理ユーザー設定 API のベースパス（管理者パス配下） */
    public static final String ADMIN_USERS = ADMIN_BASE + "/users";

    /** 管理日報ログ API のベースパス */
    public static final String ADMIN_LOGS = ADMIN_BASE + "/logs";

    /** 管理バッチ API のベースパス */
    public static final String ADMIN_BATCHES = ADMIN_BASE + "/batches";

    /** 管理セッション API のベースパス */
    public static final String ADMIN_SESSIONS = ADMIN_BASE + "/sessions";

    /** ヘルスチェック */
    public static final String HEALTH = "/health";

    private ApiPath() {
        // インスタンス化禁止
    }
}
