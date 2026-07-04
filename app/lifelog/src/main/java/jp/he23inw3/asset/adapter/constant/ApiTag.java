package jp.he23inw3.asset.adapter.constant;

/**
 * OpenAPI タグ定数。 SmallRye OpenAPI の {@code @Tag} アノテーションで使用する。
 */
public final class ApiTag {

    /** Slack Webhook 受信 */
    public static final String SLACK = "Slack";

    /** ユーザー設定管理 */
    public static final String USER_SETTINGS = "UserSettings";

    /** 対話セッション管理 */
    public static final String SESSION = "Session";

    /** システム管理・ヘルスチェック */
    public static final String SYSTEM = "System";

    /** 管理者設定管理 */
    public static final String ADMINS = "Admins";

    /** 管理用 API */
    public static final String ADMIN = "Admin";

    /** 一般ユーザー用 API */
    public static final String USER = "User";

    private ApiTag() {
        // インスタンス化禁止
    }
}
