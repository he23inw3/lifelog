package jp.he23inw3.asset.infrastructure.bigquery;

import com.google.cloud.bigquery.BigQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jp.he23inw3.asset.domain.model.HealthStatus;
import jp.he23inw3.asset.domain.repository.ExternalHealthRepository;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * BigQuery の接続状態（ヘルスチェック）を検査する専用の実装クラス。
 * <p>
 * ログの永続化リポジトリとは責務を分離し、ヘルスチェックのみを単一の責務として担います。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class BigQueryHealthRepositoryImpl implements ExternalHealthRepository {

    private final BigQuery bigQuery;

    /**
     * サービス名として "BigQuery" を返します。
     *
     * @return サービス名
     */
    @Override
    public String getServiceName() {
        return "BigQuery";
    }

    /**
     * BigQuery の疎通テストを実行し、結果を返します。
     *
     * @return 接続状態
     */
    @Override
    public HealthStatus checkHealth() {
        try {
            bigQuery.listDatasets(BigQuery.DatasetListOption.pageSize(1));
            return HealthStatus.UP;
        } catch (Exception e) {
            log.error(MessageHelper.getMessage("infra.health.bigquery.error"), e);
            return HealthStatus.DOWN;
        }
    }
}
