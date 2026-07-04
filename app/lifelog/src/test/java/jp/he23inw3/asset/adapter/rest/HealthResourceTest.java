package jp.he23inw3.asset.adapter.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import jp.he23inw3.asset.adapter.dto.HealthResponse;
import jp.he23inw3.asset.adapter.mapper.HealthMapper;
import jp.he23inw3.asset.domain.model.HealthStatus;
import jp.he23inw3.asset.usecase.HealthUseCase;
import jp.he23inw3.asset.domain.model.HealthCheckResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HealthResourceTest {

    @Mock
    HealthUseCase healthUseCase;

    @Mock
    HealthMapper healthMapper;

    HealthResource target;

    @BeforeEach
    void setUp() {
        target = new HealthResource(healthUseCase, healthMapper, "lifelog-test", "1.0.0-test");
    }

    @Test
    @DisplayName("ヘルスチェック結果がUPの場合、HTTP 200 OKを返却すること")
    void health_StatusUp_ShouldReturnOk() {
        // Arrange
        HealthCheckResult mockResult = new HealthCheckResult(HealthStatus.UP, Collections.emptyMap());
        HealthResponse mockResponse = HealthResponse.builder()
                .status(HealthStatus.UP)
                .application("lifelog-test")
                .version("1.0.0-test")
                .components(Collections.emptyMap())
                .build();

        when(healthUseCase.check()).thenReturn(mockResult);
        when(healthMapper.toResponse(mockResult, "lifelog-test", "1.0.0-test")).thenReturn(mockResponse);

        // Act
        Response response = target.health();

        // Assert
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("ヘルスチェック結果がDOWNの場合、HTTP 503 Service Unavailableを返却すること")
    void health_StatusDown_ShouldReturnServiceUnavailable() {
        // Arrange
        HealthCheckResult mockResult = new HealthCheckResult(HealthStatus.DOWN, Collections.emptyMap());
        HealthResponse mockResponse = HealthResponse.builder()
                .status(HealthStatus.DOWN)
                .application("lifelog-test")
                .version("1.0.0-test")
                .components(Collections.emptyMap())
                .build();

        when(healthUseCase.check()).thenReturn(mockResult);
        when(healthMapper.toResponse(mockResult, "lifelog-test", "1.0.0-test")).thenReturn(mockResponse);

        // Act
        Response response = target.health();

        // Assert
        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getEntity()).isEqualTo(mockResponse);
    }
}
