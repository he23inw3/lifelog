package jp.he23inw3.asset.adapter.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import jakarta.ws.rs.core.Response;
import jp.he23inw3.asset.adapter.dto.ErrorResponse;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceException;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class FaultToleranceExceptionMapperTest {

    private FaultToleranceExceptionMapper target;
    private MockedStatic<TraceHelper> traceHelperMock;

    @BeforeEach
    void setUp() {
        target = new FaultToleranceExceptionMapper();
        traceHelperMock = mockStatic(TraceHelper.class);
        traceHelperMock.when(TraceHelper::currentTraceId).thenReturn("mocked-trace-id-ft");
    }

    @AfterEach
    void tearDown() {
        traceHelperMock.close();
    }

    @Test
    @DisplayName("タイムアウト例外の発生時に HTTP 504 と GATEWAY_TIMEOUT エラー応答を返却すること")
    void toResponse_TimeoutException_ShouldReturnGatewayTimeout() {
        // Arrange
        TimeoutException exception = new TimeoutException("timeout message");

        // Act
        Response response = target.toResponse(exception);

        // Assert
        assertThat(response.getStatus()).isEqualTo(504);
        ErrorResponse body = (ErrorResponse) response.getEntity();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(504);
        assertThat(body.getErrorCode()).isEqualTo("GATEWAY_TIMEOUT");
        assertThat(body.getMessage()).isEqualTo("外部サービスとの通信がタイムアウトしました。");
        assertThat(body.getDetail()).isEqualTo("timeout message");
        assertThat(body.getTraceId()).isEqualTo("mocked-trace-id-ft");
    }

    @Test
    @DisplayName("その他のフォールトトレランス例外の発生時に HTTP 502 と GATEWAY_ERROR エラー応答を返却すること")
    void toResponse_GeneralFaultToleranceException_ShouldReturnBadGateway() {
        // Arrange
        FaultToleranceException exception = new FaultToleranceException("circuit breaker open");

        // Act
        Response response = target.toResponse(exception);

        // Assert
        assertThat(response.getStatus()).isEqualTo(502);
        ErrorResponse body = (ErrorResponse) response.getEntity();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(502);
        assertThat(body.getErrorCode()).isEqualTo("GATEWAY_ERROR");
        assertThat(body.getMessage()).isEqualTo("外部サービスとの通信でエラーが発生しました。");
        assertThat(body.getDetail()).isEqualTo("circuit breaker open");
        assertThat(body.getTraceId()).isEqualTo("mocked-trace-id-ft");
    }
}
