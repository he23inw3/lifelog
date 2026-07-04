package jp.he23inw3.asset.adapter.provider;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jp.he23inw3.asset.adapter.constant.ErrorCode;
import jp.he23inw3.asset.adapter.dto.ErrorResponse;
import jp.he23inw3.asset.domain.exception.InvalidRequestException;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * リクエスト検証例外を HTTP 400 Bad Request にマッピングする ExceptionMapper。
 */
@Slf4j
@Provider
public class InvalidRequestExceptionMapper implements ExceptionMapper<InvalidRequestException> {

    @Override
    public Response toResponse(InvalidRequestException exception) {
        log.warn(MessageHelper.getMessage("adapter.provider.validation.log", exception.getMessage()));

        ErrorResponse body = ErrorResponse.builder()
                .status(Response.Status.BAD_REQUEST.getStatusCode())
                .errorCode(ErrorCode.BAD_REQUEST)
                .message(exception.getMessage())
                .traceId(TraceHelper.currentTraceId())
                .build();

        return Response.status(Response.Status.BAD_REQUEST).entity(body).build();
    }
}
