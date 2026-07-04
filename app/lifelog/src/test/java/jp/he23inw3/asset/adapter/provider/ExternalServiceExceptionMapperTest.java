package jp.he23inw3.asset.adapter.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import jakarta.ws.rs.core.Response;
import jp.he23inw3.asset.adapter.constant.ErrorCode;
import jp.he23inw3.asset.adapter.dto.ErrorResponse;
import jp.he23inw3.asset.domain.exception.ExternalServiceException;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ExternalServiceExceptionMapperTest {

    private ExternalServiceExceptionMapper target;
    private MockedStatic<TraceHelper> traceHelperMock;

    @BeforeEach
    void setUp() {
        target = new ExternalServiceExceptionMapper();
        traceHelperMock = mockStatic(TraceHelper.class);
        traceHelperMock.when(TraceHelper::currentTraceId).thenReturn("mocked-trace-id-54321");
    }

    @AfterEach
    void tearDown() {
        traceHelperMock.close();
    }

    @Test
    @DisplayName("外部サービス連携例外の発生時に HTTP 502 と TraceID を含むエラー応答を返却すること")
    void toResponse_ShouldReturnBadGatewayWithErrorResponseAndTraceId() {
        // Arrange
        ExternalServiceException exception = new ExternalServiceException("Slack API connection timeout");

        // Act
        Response response = target.toResponse(exception);

        // Assert
        assertThat(response.getStatus()).isEqualTo(502);
        ErrorResponse body = (ErrorResponse) response.getEntity();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(502);
        assertThat(body.getErrorCode()).isEqualTo(ErrorCode.EXTERNAL_SERVICE_ERROR);
        assertThat(body.getDetail()).isEqualTo("Slack API connection timeout");
        assertThat(body.getTraceId()).isEqualTo("mocked-trace-id-54321");
    }
}
