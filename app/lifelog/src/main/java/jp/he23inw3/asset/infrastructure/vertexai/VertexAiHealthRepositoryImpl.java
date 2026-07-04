package jp.he23inw3.asset.infrastructure.vertexai;

import com.google.genai.Client;
import jakarta.enterprise.context.ApplicationScoped;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.model.HealthStatus;
import jp.he23inw3.asset.domain.repository.ExternalHealthRepository;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Vertex AI Gemini API の接続状態（ヘルスチェック）を検査する専用の実装クラス。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class VertexAiHealthRepositoryImpl implements ExternalHealthRepository {

    private final LifeLogConfig config;

    private final Client client;

    /**
     * サービス名として "Vertex AI" を返します。
     *
     * @return サービス名
     */
    @Override
    public String getServiceName() {
        return "Vertex AI";
    }

    /**
     * Vertex AI Gemini API の疎通テストを実行し、結果を返します。
     *
     * @return 接続状態
     */
    @Override
    public HealthStatus checkHealth() {
        String modelName = config.gemini().model();

        try {
            client.models.generateContent(modelName, "ping", null);
            return HealthStatus.UP;
        } catch (Exception e) {
            log.error(MessageHelper.getMessage("infra.health.vertexai.error"), e);
            return HealthStatus.DOWN;
        }
    }
}
