package jp.he23inw3.asset.adapter.dto;

import java.util.Map;
import jp.he23inw3.asset.domain.model.HealthStatus;
import lombok.Builder;
import lombok.Data;

/**
 * ヘルスチェック API (GET /health) レスポンス DTO。
 */
@Data
@Builder
public class HealthResponse {

    /** システム全体の状態 */
    private HealthStatus status;

    /** アプリケーション名 */
    private String application;

    /** バージョン */
    private String version;

    /**
     * 個別サービスの疎通状態。 キーはサービス名（例: {@code "bigquery"}, {@code "firestore"}）、
     * 値はヘルスステータス。
     */
    private Map<String, HealthStatus> components;
}
