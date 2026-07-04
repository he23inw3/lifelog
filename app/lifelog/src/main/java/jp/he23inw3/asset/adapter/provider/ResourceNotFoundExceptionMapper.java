package jp.he23inw3.asset.adapter.provider;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jp.he23inw3.asset.adapter.constant.ErrorCode;
import jp.he23inw3.asset.adapter.dto.ErrorResponse;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * リソース未存在例外を HTTP 404 Not Found にマッピングする ExceptionMapper。
 */
@Slf4j
@Provider
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

    @Override
    public Response toResponse(ResourceNotFoundException exception) {
        log.warn(MessageHelper.getMessage("adapter.provider.notfound.log", exception.getMessage()));

        ErrorResponse body = ErrorResponse.builder()
                .status(Response.Status.NOT_FOUND.getStatusCode())
                .errorCode(ErrorCode.NOT_FOUND)
                .message(exception.getMessage())
                .traceId(TraceHelper.currentTraceId())
                .build();

        return Response.status(Response.Status.NOT_FOUND).entity(body).build();
    }
}
