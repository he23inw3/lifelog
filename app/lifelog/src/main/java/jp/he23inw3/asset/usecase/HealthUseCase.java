package jp.he23inw3.asset.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import java.util.LinkedHashMap;
import java.util.Map;
import jp.he23inw3.asset.domain.model.HealthCheckResult;
import jp.he23inw3.asset.domain.model.HealthStatus;
import jp.he23inw3.asset.domain.repository.ExternalHealthRepository;
import lombok.RequiredArgsConstructor;

/**
 * ヘルスチェックのユースケース。
 * <p>
 * CDI の {@link Instance} により登録されているすべての {@link ExternalHealthRepository}
 * を動的に取得して実行し、結果を集計する。
 * <p>
 * 新しい外部サービスの疎通確認を追加する場合は {@link ExternalHealthRepository} を実装した
 * {@code @ApplicationScoped} クラスを追加するだけでよく、このクラスの変更は不要。
 */
@ApplicationScoped
@RequiredArgsConstructor
public class HealthUseCase {

    private final Instance<ExternalHealthRepository> healthRepositories;

    /**
     * 登録されているすべての外部サービスの疎通確認を実行し、結果を集計する。
     *
     * @return ヘルスチェック結果（全体ステータス + 個別サービス状態マップ）
     */
    public HealthCheckResult check() {
        Map<String, HealthStatus> components = new LinkedHashMap<>();

        for (ExternalHealthRepository repo : healthRepositories) {
            HealthStatus status = repo.checkHealth();
            components.put(repo.getServiceName(), status != null ? status : HealthStatus.DOWN);
        }

        boolean isAllUp = components.values().stream().allMatch(status -> status == HealthStatus.UP);
        HealthStatus overallStatus = isAllUp ? HealthStatus.UP : HealthStatus.DOWN;

        return new HealthCheckResult(overallStatus, components);
    }
}
