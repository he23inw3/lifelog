package jp.he23inw3.asset.infrastructure.firestore;

import com.google.cloud.firestore.Firestore;
import jakarta.enterprise.context.ApplicationScoped;
import jp.he23inw3.asset.domain.model.HealthStatus;
import jp.he23inw3.asset.domain.repository.ExternalHealthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Firestore の接続状態（ヘルスチェック）を検査する専用の実装クラス。
 * <p>
 * 設定やセッションの永続化リポジトリとは責務を分離し、ヘルスチェックのみを単一の責務として担います。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class FirestoreHealthRepositoryImpl implements ExternalHealthRepository {

    private final Firestore firestore;

    /**
     * サービス名として "Firestore" を返します。
     *
     * @return サービス名
     */
    @Override
    public String getServiceName() {
        return "Firestore";
    }

    /**
     * Firestore の疎通テストを実行し、結果を返します。
     *
     * @return 接続状態
     */
    @Override
    public HealthStatus checkHealth() {
        try {
            firestore.collection("health_check").document("status").get().get();
            return HealthStatus.UP;
        } catch (Exception e) {
            log.error("Firestore health check failed", e);
            return HealthStatus.DOWN;
        }
    }
}
