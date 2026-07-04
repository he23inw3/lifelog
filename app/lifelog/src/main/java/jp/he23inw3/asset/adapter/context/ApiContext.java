package jp.he23inw3.asset.adapter.context;

import jakarta.enterprise.context.RequestScoped;
import lombok.Data;

/**
 * リクエストスコープのコンテキスト情報。
 * <p>
 * フィルター層で認証済みユーザー情報を設定し、 リソースクラス・ユースケース層で参照可能にする。
 */
@Data
@RequestScoped
public class ApiContext {

    /**
     * OIDC トークンから取得した認証済みユーザーの識別子。 Slack イベント受信エンドポイントでは未設定（null）になる場合がある。
     */
    private String authenticatedUserId;

    /**
     * リクエストの追跡 ID（ロギング・分散トレーシング用）。 OpenTelemetry の TraceID を格納する。
     */
    private String traceId;
}
