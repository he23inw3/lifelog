package jp.he23inw3.asset.adapter.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import jakarta.ws.rs.core.Response;
import jp.he23inw3.asset.adapter.dto.ErrorResponse;
import jp.he23inw3.asset.domain.exception.AlreadyExistsException;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class AlreadyExistsExceptionMapperTest {

    private AlreadyExistsExceptionMapper target;
    private MockedStatic<TraceHelper> traceHelperMock;

    @BeforeEach
    void setUp() {
        target = new AlreadyExistsExceptionMapper();
        traceHelperMock = mockStatic(TraceHelper.class);
        traceHelperMock.when(TraceHelper::currentTraceId).thenReturn("mocked-trace-id-12345");
    }

    @AfterEach
    void tearDown() {
        traceHelperMock.close();
    }

    @Test
    @DisplayName("リソース重複例外発生時に HTTP 409 と TraceID を含むエラー応答を返却すること")
    void toResponse_ShouldReturnConflictWithErrorResponseAndTraceId() {
        AlreadyExistsException exception = new AlreadyExistsException("Resource already exists");

        Response response = target.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(409);
        ErrorResponse body = (ErrorResponse) response.getEntity();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(409);
        assertThat(body.getErrorCode()).isEqualTo("CONFLICT");
        assertThat(body.getMessage()).isEqualTo("Resource already exists");
        assertThat(body.getTraceId()).isEqualTo("mocked-trace-id-12345");
    }
}
