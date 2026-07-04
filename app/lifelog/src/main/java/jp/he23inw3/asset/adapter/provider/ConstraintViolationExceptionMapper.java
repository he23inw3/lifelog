package jp.he23inw3.asset.adapter.provider;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.stream.Collectors;
import jp.he23inw3.asset.adapter.constant.ErrorCode;
import jp.he23inw3.asset.adapter.dto.ErrorResponse;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * Bean Validation の制約違反例外を HTTP 400 Bad Request にマッピングする ExceptionMapper。
 */
@Slf4j
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String detail = exception.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining(", "));

        log.warn(MessageHelper.getMessage("adapter.provider.validation.log", detail));

        ErrorResponse body = ErrorResponse.builder()
                .status(Response.Status.BAD_REQUEST.getStatusCode())
                .errorCode(ErrorCode.VALIDATION_ERROR)
                .message(MessageHelper.getMessage("adapter.provider.validation.message"))
                .detail(detail)
                .traceId(TraceHelper.currentTraceId())
                .build();

        return Response.status(Response.Status.BAD_REQUEST).entity(body).build();
    }
}
