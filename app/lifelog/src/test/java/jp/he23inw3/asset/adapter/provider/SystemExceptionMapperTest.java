package jp.he23inw3.asset.adapter.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import jakarta.ws.rs.core.Response;
import jp.he23inw3.asset.adapter.dto.ErrorResponse;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class SystemExceptionMapperTest {

    private SystemExceptionMapper target;
    private MockedStatic<TraceHelper> traceHelperMock;

    @BeforeEach
    void setUp() {
        target = new SystemExceptionMapper();
        traceHelperMock = mockStatic(TraceHelper.class);
        traceHelperMock.when(TraceHelper::currentTraceId).thenReturn("mocked-trace-id-12345");
    }

    @AfterEach
    void tearDown() {
        traceHelperMock.close();
    }

    @Test
    @DisplayName("システム内部例外の発生時にメッセージ付きで HTTP 500 と TraceID を含むエラー応答を返却すること")
    void toResponse_ShouldReturnInternalServerErrorWithMessageAndTraceId() {
        Exception exception = new RuntimeException("Database connection failure");

        Response response = target.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(500);
        ErrorResponse body = (ErrorResponse) response.getEntity();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(500);
        assertThat(body.getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(body.getMessage()).isEqualTo("Database connection failure");
        assertThat(body.getDetail()).isEqualTo("Database connection failure");
        assertThat(body.getTraceId()).isEqualTo("mocked-trace-id-12345");
    }

    @Test
    @DisplayName("システム内部例外の発生時にメッセージがない場合にデフォルトメッセージで HTTP 500 と TraceID を含むエラー応答を返却すること")
    void toResponse_WithNoMessage_ShouldReturnInternalServerErrorWithDefaultMessageAndTraceId() {
        Exception exception = new RuntimeException();

        Response response = target.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(500);
        ErrorResponse body = (ErrorResponse) response.getEntity();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(500);
        assertThat(body.getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(body.getMessage()).isEqualTo("システムエラーが発生しました。時間をおいて再度お試しください。");
        assertThat(body.getDetail()).isEqualTo("システムエラーが発生しました。時間をおいて再度お試しください。");
        assertThat(body.getTraceId()).isEqualTo("mocked-trace-id-12345");
    }
}
