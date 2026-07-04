package jp.he23inw3.asset.adapter.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import jakarta.ws.rs.core.Response;
import jp.he23inw3.asset.adapter.constant.ErrorCode;
import jp.he23inw3.asset.adapter.dto.ErrorResponse;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ResourceNotFoundExceptionMapperTest {

    private ResourceNotFoundExceptionMapper target;
    private MockedStatic<TraceHelper> traceHelperMock;

    @BeforeEach
    void setUp() {
        target = new ResourceNotFoundExceptionMapper();
        traceHelperMock = mockStatic(TraceHelper.class);
        traceHelperMock.when(TraceHelper::currentTraceId).thenReturn("mocked-trace-id-12345");
    }

    @AfterEach
    void tearDown() {
        traceHelperMock.close();
    }

    @Test
    @DisplayName("リソース未存在例外の発生時に HTTP 404 と TraceID を含むエラー応答を返却すること")
    void toResponse_ShouldReturnNotFoundWithErrorResponseAndTraceId() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("User not found");

        // Act
        Response response = target.toResponse(exception);

        // Assert
        assertThat(response.getStatus()).isEqualTo(404);
        ErrorResponse body = (ErrorResponse) response.getEntity();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(404);
        assertThat(body.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        assertThat(body.getMessage()).isEqualTo("User not found");
        assertThat(body.getTraceId()).isEqualTo("mocked-trace-id-12345");
    }
}
