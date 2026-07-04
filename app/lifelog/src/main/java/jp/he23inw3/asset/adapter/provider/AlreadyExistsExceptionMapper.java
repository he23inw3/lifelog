package jp.he23inw3.asset.adapter.provider;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jp.he23inw3.asset.adapter.constant.ErrorCode;
import jp.he23inw3.asset.adapter.dto.ErrorResponse;
import jp.he23inw3.asset.domain.exception.AlreadyExistsException;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * リソース重複存在例外を HTTP 409 Conflict にマッピングする ExceptionMapper。
 */
@Slf4j
@Provider
public class AlreadyExistsExceptionMapper implements ExceptionMapper<AlreadyExistsException> {

    @Override
    public Response toResponse(AlreadyExistsException exception) {
        log.warn(MessageHelper.getMessage("adapter.provider.conflict.log", exception.getMessage()));

        ErrorResponse body = ErrorResponse.builder()
                .status(Response.Status.CONFLICT.getStatusCode())
                .errorCode(ErrorCode.CONFLICT)
                .message(exception.getMessage())
                .traceId(TraceHelper.currentTraceId())
                .build();

        return Response.status(Response.Status.CONFLICT).entity(body).build();
    }
}
