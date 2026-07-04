package jp.he23inw3.asset.adapter.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import jakarta.ws.rs.core.Response;
import jp.he23inw3.asset.adapter.constant.ErrorCode;
import jp.he23inw3.asset.adapter.dto.ErrorResponse;
import jp.he23inw3.asset.domain.exception.InvalidRegistrationException;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class InvalidRegistrationExceptionMapperTest {

    private InvalidRegistrationExceptionMapper target;
    private MockedStatic<TraceHelper> traceHelperMock;

    @BeforeEach
    void setUp() {
        target = new InvalidRegistrationExceptionMapper();
        traceHelperMock = mockStatic(TraceHelper.class);
        traceHelperMock.when(TraceHelper::currentTraceId).thenReturn("mocked-trace-id-12345");
    }

    @AfterEach
    void tearDown() {
        traceHelperMock.close();
    }

    @Test
    @DisplayName("ユーザー初回登録バリデーション例外発生時に HTTP 400 と TraceID を含むエラー応答を返却すること")
    void toResponse_ShouldReturnBadRequestWithErrorResponseAndTraceId() {
        InvalidRegistrationException exception = new InvalidRegistrationException("Invalid email format");

        Response response = target.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(400);
        ErrorResponse body = (ErrorResponse) response.getEntity();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
        assertThat(body.getMessage()).isEqualTo("Invalid email format");
        assertThat(body.getTraceId()).isEqualTo("mocked-trace-id-12345");
    }
}
