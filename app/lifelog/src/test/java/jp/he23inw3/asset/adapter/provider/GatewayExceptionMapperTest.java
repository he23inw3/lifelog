package jp.he23inw3.asset.adapter.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import jakarta.ws.rs.core.Response;
import jp.he23inw3.asset.adapter.dto.ErrorResponse;
import jp.he23inw3.asset.domain.exception.GatewayException;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class GatewayExceptionMapperTest {

    private GatewayExceptionMapper target;
    private MockedStatic<TraceHelper> traceHelperMock;

    @BeforeEach
    void setUp() {
        target = new GatewayExceptionMapper();
        traceHelperMock = mockStatic(TraceHelper.class);
        traceHelperMock.when(TraceHelper::currentTraceId).thenReturn("mocked-trace-id-gateway");
    }

    @AfterEach
    void tearDown() {
        traceHelperMock.close();
    }

    @Test
    @DisplayName("ゲートウェイ通信例外の発生時に HTTP 502 と TraceID を含むエラー応答を返却すること")
    void toResponse_ShouldReturnBadGatewayWithErrorResponseAndTraceId() {
        // Arrange
        GatewayException exception = new GatewayException("Slack connection failed");

        // Act
        Response response = target.toResponse(exception);

        // Assert
        assertThat(response.getStatus()).isEqualTo(502);
        ErrorResponse body = (ErrorResponse) response.getEntity();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(502);
        assertThat(body.getErrorCode()).isEqualTo("GATEWAY_ERROR");
        assertThat(body.getDetail()).isEqualTo("Slack connection failed");
        assertThat(body.getTraceId()).isEqualTo("mocked-trace-id-gateway");
    }
}
