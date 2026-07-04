package jp.he23inw3.asset.configuration;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;
import java.util.Optional;

/**
 * アプリケーション (LifeLog) の共通設定値を保持するインターフェース。
 * <p>
 * {@code application.yaml} 等で {@code lifelog} プレフィックス配下に定義された設定を タイプセーフにバインドして取得します。
 */
@ConfigMapping(prefix = "lifelog")
public interface LifeLogConfig {

    /**
     * Google Cloud 共通設定値の定義インターフェース。
     */
    interface Google {
        /**
         * GCP プロジェクト ID。
         *
         * @return GCP プロジェクト ID
         */
        @WithName("project-id")
        @WithDefault("local-project")
        String projectId();

        /**
         * 日本の祝日情報を取得するための Google カレンダー ID。
         *
         * @return 日本の祝日カレンダー ID
         */
        @WithName("japanese-holiday-calendar-id")
        @WithDefault("ja.japanese#holiday@group.v.calendar.google.com")
        String japaneseHolidayCalendarId();

        /**
         * Google OAuth 2.0 クライアント ID。
         *
         * @return クライアント ID
         */
        @WithName("oauth-client-id")
        @WithDefault("dummy-google-client-id")
        String oauthClientId();

        /**
         * Google OAuth 2.0 クライアントシークレット。
         *
         * @return クライアントシークレット
         */
        @WithName("oauth-client-secret")
        @WithDefault("dummy-google-client-secret")
        String oauthClientSecret();

        /**
         * Google OAuth 2.0 リダイレクト URI。
         *
         * @return リダイレクト URI
         */
        @WithName("oauth-redirect-uri")
        @WithDefault("http://localhost:5000/api/v1/auth/google/callback")
        String oauthRedirectUri();
    }

    /**
     * Vertex AI Gemini 連携用設定値の定義インターフェース。
     */
    interface Gemini {
        /**
         * Vertex AI の API ロケーション。
         *
         * @return API ロケーション
         */
        @WithDefault("us-central1")
        String location();

        /**
         * 使用する Gemini モデル名。
         *
         * @return モデル名
         */
        @WithDefault("gemini-2.5-flash-lite")
        String model();
    }

    /**
     * Slack 連携用設定値の定義インターフェース。
     */
    interface Slack {
        /**
         * Slack からの Webhook 署名検証用シークレット。
         *
         * @return 署名検証用シークレット
         */
        @WithName("signing-secret")
        @WithDefault("dummy-signing-secret")
        String signingSecret();

        /**
         * Slack Bot トークン (xoxb-...)。
         *
         * @return Slack Bot トークン
         */
        @WithName("bot-token")
        @WithDefault("xoxb-dummy")
        String botToken();
    }

    /**
     * BigQuery 連携用設定値の定義インターフェース。
     */
    interface Bigquery {
        /**
         * 日報ログを書き込む BigQuery データセット名。
         *
         * @return データセット名
         */
        @WithDefault("lifelog_dataset")
        String dataset();
    }

    /**
     * Google Cloud 共通設定を取得します。
     *
     * @return Google Cloud 設定
     */
    Google google();

    /**
     * Vertex AI Gemini 連携用の設定を取得します。
     *
     * @return Gemini 設定
     */
    Gemini gemini();

    /**
     * Slack API 連携用の設定を取得します。
     *
     * @return Slack 設定
     */
    Slack slack();

    /**
     * BigQuery テーブル等の設定を取得します。
     *
     * @return BigQuery 設定
     */
    Bigquery bigquery();

    /**
     * ポータル設定の定義インターフェース。
     */
    interface Portal {
        /**
         * ポータルのベース URL。
         *
         * @return ポータルベース URL
         */
        @WithName("base-url")
        @WithDefault("http://localhost:5173")
        String baseUrl();
    }

    /**
     * ポータル設定を取得します。
     *
     * @return ポータル設定
     */
    Portal portal();

    /**
     * 環境識別用変数 (dev / prod)
     *
     * @return 環境識別文字列
     */
    @WithName("app-node")
    @WithDefault("dev")
    String appNode();

    /**
     * デモモード設定。
     */
    interface Demo {
        /**
         * デモモード有効フラグ。 true の場合、Google OAuth をスキップし、擬似 Calendar / Slack を使用します。
         *
         * @return デモモード有効の場合 true
         */
        @WithDefault("false")
        boolean enabled();

        /**
         * デモユーザーの OIDC メールアドレス（デモ認証バイパス時に使用）。
         *
         * @return デモユーザーメール
         */
        @WithName("user-email")
        @WithDefault("demo@example.com")
        String userEmail();

        /**
         * デモユーザーの Slack ユーザー ID。
         *
         * @return デモ Slack ユーザー ID
         */
        @WithName("slack-user-id")
        @WithDefault("DEMO_USER")
        String slackUserId();
    }

    /**
     * デモモード設定を取得します。
     *
     * @return デモモード設定
     */
    Demo demo();

    /**
     * 暗号化・復号用設定値の定義インターフェース。
     */
    interface Crypto {
        /**
         * 暗号化共通鍵の値。
         *
         * @return 暗号化鍵の値
         */
        @WithName("key-value")
        @WithDefault("1234567890123456")
        String keyValue();
    }

    /**
     * 暗号化・復号用の設定を取得します。
     *
     * @return 暗号化設定
     */
    Crypto crypto();

    /**
     * ブートストラップ設定を取得します。
     *
     * @return ブートストラップ設定
     */
    Bootstrap bootstrap();

    /**
     * ブートストラップ設定の定義インターフェース。
     */
    interface Bootstrap {
        /**
         * 管理者が0人の場合に初回登録（ブートストラップ）を許可するメールアドレス。 未設定の場合は空として扱い、ブートストラップ不可となる。 環境変数 BOOTSTRAP_ALLOWED_EMAIL で指定することも可能。
         *
         * @return 許可メールアドレス。未設定の場合は {@link Optional#empty()}
         */
        @WithName("allowed-email")
        Optional<String> allowedEmail();
    }
}
