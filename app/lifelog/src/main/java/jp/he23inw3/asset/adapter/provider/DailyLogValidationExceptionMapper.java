package jp.he23inw3.asset.adapter.provider;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jp.he23inw3.asset.adapter.constant.ErrorCode;
import jp.he23inw3.asset.adapter.dto.ErrorResponse;
import jp.he23inw3.asset.domain.exception.DailyLogValidationException;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * 日報バリデーション例外を HTTP 400 Bad Request にマッピングする ExceptionMapper。
 */
@Slf4j
@Provider
public class DailyLogValidationExceptionMapper implements ExceptionMapper<DailyLogValidationException> {

    @Override
    public Response toResponse(DailyLogValidationException exception) {
        log.warn("Daily log validation failed: {}", exception.getMessage());

        ErrorResponse body = ErrorResponse.builder()
                .status(Response.Status.BAD_REQUEST.getStatusCode())
                .errorCode(ErrorCode.VALIDATION_ERROR)
                .message(exception.getMessage())
                .traceId(TraceHelper.currentTraceId())
                .build();

        return Response.status(Response.Status.BAD_REQUEST).entity(body).build();
    }
}
