package jp.he23inw3.asset.adapter.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import jp.he23inw3.asset.adapter.dto.HealthResponse;
import jp.he23inw3.asset.domain.model.HealthStatus;
import jp.he23inw3.asset.domain.model.HealthCheckResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class HealthMapperTest {

    private final HealthMapper mapper = Mappers.getMapper(HealthMapper.class);

    @Nested
    @DisplayName("HealthCheckResultからHealthResponseへの変換")
    class ToResponse {

        @Test
        @DisplayName("アプリ情報およびコンポーネントのヘルス状態が正しくマッピングされること")
        void toResponse_ShouldMapAllFields() {
            // Arrange
            Map<String, HealthStatus> components = new HashMap<>();
            components.put("firestore", HealthStatus.UP);
            components.put("slack", HealthStatus.DOWN);

            HealthCheckResult result = new HealthCheckResult(HealthStatus.UP, components);
            String appName = "lifelog-app";
            String appVersion = "1.0.0-SNAPSHOT";

            // Act
            HealthResponse response = mapper.toResponse(result, appName, appVersion);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(HealthStatus.UP);
            assertThat(response.getApplication()).isEqualTo("lifelog-app");
            assertThat(response.getVersion()).isEqualTo("1.0.0-SNAPSHOT");
            assertThat(response.getComponents()).containsAllEntriesOf(components);
        }

        @Test
        @DisplayName("結果情報(result)がnullの場合でもアプリケーション情報のみマッピングされること")
        void toResponse_WithNullResult_ShouldStillMapAppInfo() {
            // Act
            HealthResponse response = mapper.toResponse(null, "app", "version");

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isNull();
            assertThat(response.getComponents()).isNull();
            assertThat(response.getApplication()).isEqualTo("app");
            assertThat(response.getVersion()).isEqualTo("version");
        }

        @Test
        @DisplayName("すべてのパラメータがnullの場合はnullが返ること")
        void toResponse_WithAllNull_ShouldReturnNull() {
            // Act
            HealthResponse response = mapper.toResponse(null, null, null);

            // Assert
            assertThat(response).isNull();
        }
    }
}
