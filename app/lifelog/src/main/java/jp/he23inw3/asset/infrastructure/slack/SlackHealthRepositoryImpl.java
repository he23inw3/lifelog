package jp.he23inw3.asset.infrastructure.slack;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.auth.AuthTestResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.model.HealthStatus;
import jp.he23inw3.asset.domain.repository.ExternalHealthRepository;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Slack API の接続状態（ヘルスチェック）を検査する専用の実装クラス。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SlackHealthRepositoryImpl implements ExternalHealthRepository {

    private final LifeLogConfig config;

    private final Slack slack;

    /**
     * サービス名として "Slack" を返します。
     *
     * @return サービス名
     */
    @Override
    public String getServiceName() {
        return "Slack";
    }

    /**
     * Slack API の疎通テストを実行し、結果を返します。
     *
     * @return 接続状態
     */
    @Override
    public HealthStatus checkHealth() {
        try {
            MethodsClient methodsClient = slack.methods(config.slack().botToken());
            AuthTestResponse response = methodsClient.authTest(r -> r);
            if (response.isOk()) {
                return HealthStatus.UP;
            }

            log.error(MessageHelper.getMessage("infra.health.slack.error.response", response.getError()));
            return HealthStatus.DOWN;
        } catch (Exception e) {
            log.error(MessageHelper.getMessage("infra.health.slack.error"), e);
            return HealthStatus.DOWN;
        }
    }
}
