package jp.he23inw3.asset.adapter.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import jakarta.ws.rs.core.Response;
import jp.he23inw3.asset.adapter.constant.ErrorCode;
import jp.he23inw3.asset.adapter.dto.ErrorResponse;
import jp.he23inw3.asset.domain.exception.DemoModeDisabledException;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class DemoModeDisabledExceptionMapperTest {

    private DemoModeDisabledExceptionMapper target;
    private MockedStatic<TraceHelper> traceHelperMock;

    @BeforeEach
    void setUp() {
        target = new DemoModeDisabledExceptionMapper();
        traceHelperMock = mockStatic(TraceHelper.class);
        traceHelperMock.when(TraceHelper::currentTraceId).thenReturn("mocked-trace-id-12345");
    }

    @AfterEach
    void tearDown() {
        traceHelperMock.close();
    }

    @Test
    @DisplayName("デモモード無効例外発生時に HTTP 403 と TraceID を含むエラー応答を返却すること")
    void toResponse_ShouldReturnForbiddenWithErrorResponseAndTraceId() {
        DemoModeDisabledException exception = new DemoModeDisabledException("Demo mode is disabled");

        Response response = target.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(403);
        ErrorResponse body = (ErrorResponse) response.getEntity();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(403);
        assertThat(body.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
        assertThat(body.getMessage()).isEqualTo("Demo mode is disabled");
        assertThat(body.getTraceId()).isEqualTo("mocked-trace-id-12345");
    }
}
