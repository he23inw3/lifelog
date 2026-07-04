package jp.he23inw3.asset.adapter.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import java.util.Set;
import jp.he23inw3.asset.adapter.constant.ErrorCode;
import jp.he23inw3.asset.adapter.dto.ErrorResponse;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ConstraintViolationExceptionMapperTest {

    private ConstraintViolationExceptionMapper target;
    private MockedStatic<TraceHelper> traceHelperMock;

    @BeforeEach
    void setUp() {
        target = new ConstraintViolationExceptionMapper();
        traceHelperMock = mockStatic(TraceHelper.class);
        traceHelperMock.when(TraceHelper::currentTraceId).thenReturn("mocked-trace-id-99999");
    }

    @AfterEach
    void tearDown() {
        traceHelperMock.close();
    }

    @Test
    @DisplayName("制約違反例外の発生時に HTTP 400 と TraceID を含むエラー応答を返却すること")
    void toResponse_ShouldReturnBadRequestWithErrorResponseAndTraceId() {
        // Arrange
        ConstraintViolationException exception = mock(ConstraintViolationException.class);
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(path.toString()).thenReturn("userName");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("ユーザー名は必須です");

        // Use unchecked assignment safely in test
        Set<ConstraintViolation<?>> violations = Set.of(violation);
        doReturn(violations).when(exception).getConstraintViolations();

        // Act
        Response response = target.toResponse(exception);

        // Assert
        assertThat(response.getStatus()).isEqualTo(400);
        ErrorResponse body = (ErrorResponse) response.getEntity();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
        assertThat(body.getDetail()).isEqualTo("userName: ユーザー名は必須です");
        assertThat(body.getTraceId()).isEqualTo("mocked-trace-id-99999");
    }
}
