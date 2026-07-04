package jp.he23inw3.asset.domain.repository;

import jp.he23inw3.asset.domain.model.HealthStatus;

/**
 * 外部サービスのヘルスチェック（疎通確認）を定義するインターフェース。
 * <p>
 * Firestore や BigQuery など、外部リソースへの接続状態を検査するリポジトリは このインターフェースを実装します。
 */
public interface ExternalHealthRepository {

    /**
     * サービスの名前を取得します。
     *
     * @return サービス名 (例: "Firestore", "BigQuery")
     */
    String getServiceName();

    /**
     * サービスの接続状態を検査し、ステータスを返します。
     *
     * @return 接続状態
     */
    HealthStatus checkHealth();
}
