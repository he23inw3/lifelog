package jp.he23inw3.asset.adapter.provider;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jp.he23inw3.asset.adapter.constant.ErrorCode;
import jp.he23inw3.asset.adapter.dto.ErrorResponse;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * システム内部例外を HTTP 500 Internal Server Error にマッピングする ExceptionMapper。
 */
@Slf4j
@Provider
public class SystemExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        log.error(MessageHelper.getMessage("adapter.provider.system.log", exception.getMessage()), exception);

        String message = StringUtils.defaultIfEmpty(exception.getMessage(), "システムエラーが発生しました。時間をおいて再度お試しください。");
        ErrorResponse body = ErrorResponse.builder()
                .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR)
                .message(message)
                .detail(message)
                .traceId(TraceHelper.currentTraceId())
                .build();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(body).build();
    }
}
