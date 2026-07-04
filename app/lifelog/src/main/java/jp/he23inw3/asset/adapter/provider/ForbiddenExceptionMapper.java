package jp.he23inw3.asset.adapter.provider;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jp.he23inw3.asset.adapter.constant.ErrorCode;
import jp.he23inw3.asset.adapter.dto.ErrorResponse;
import jp.he23inw3.asset.domain.exception.ForbiddenException;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * 認可例外を HTTP 403 Forbidden にマッピングする ExceptionMapper。
 */
@Slf4j
@Provider
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {

    @Override
    public Response toResponse(ForbiddenException exception) {
        log.warn(MessageHelper.getMessage("adapter.provider.forbidden.log", exception.getMessage()));

        ErrorResponse body = ErrorResponse.builder()
                .status(Response.Status.FORBIDDEN.getStatusCode())
                .errorCode(ErrorCode.FORBIDDEN)
                .message(exception.getMessage())
                .traceId(TraceHelper.currentTraceId())
                .build();

        return Response.status(Response.Status.FORBIDDEN).entity(body).build();
    }
}
