package jp.he23inw3.asset.adapter.provider;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jp.he23inw3.asset.adapter.constant.ErrorCode;
import jp.he23inw3.asset.adapter.dto.ErrorResponse;
import jp.he23inw3.asset.domain.exception.GatewayException;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * ゲートウェイ通信例外を HTTP 502 Bad Gateway にマッピングする ExceptionMapper。
 */
@Slf4j
@Provider
public class GatewayExceptionMapper implements ExceptionMapper<GatewayException> {

    @Override
    public Response toResponse(GatewayException exception) {
        log.error(MessageHelper.getMessage("adapter.provider.external.log", exception.getMessage()), exception);

        ErrorResponse body = ErrorResponse.builder()
                .status(Response.Status.BAD_GATEWAY.getStatusCode())
                .errorCode(ErrorCode.GATEWAY_ERROR)
                .message(MessageHelper.getMessage("adapter.provider.external.message"))
                .detail(exception.getMessage())
                .traceId(TraceHelper.currentTraceId())
                .build();

        return Response.status(Response.Status.BAD_GATEWAY).entity(body).build();
    }
}
