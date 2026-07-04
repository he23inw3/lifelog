package jp.he23inw3.asset.adapter.provider;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jp.he23inw3.asset.adapter.constant.ErrorCode;
import jp.he23inw3.asset.adapter.dto.ErrorResponse;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceException;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;

/**
 * MicroProfile Fault Tolerance 例外（タイムアウト等）を適切な HTTP ステータスコードにマッピングする
 * ExceptionMapper。
 */
@Slf4j
@Provider
public class FaultToleranceExceptionMapper implements ExceptionMapper<FaultToleranceException> {

    @Override
    public Response toResponse(FaultToleranceException exception) {
        log.error("Fault Tolerance error occurred: {}", exception.getMessage(), exception);

        int status;
        String errorCode;
        String message;

        if (exception instanceof TimeoutException) {
            status = Response.Status.GATEWAY_TIMEOUT.getStatusCode();
            errorCode = ErrorCode.GATEWAY_TIMEOUT;
            message = "外部サービスとの通信がタイムアウトしました。";
        } else {
            status = Response.Status.BAD_GATEWAY.getStatusCode();
            errorCode = ErrorCode.GATEWAY_ERROR;
            message = "外部サービスとの通信でエラーが発生しました。";
        }

        ErrorResponse body = ErrorResponse.builder()
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .detail(exception.getMessage())
                .traceId(TraceHelper.currentTraceId())
                .build();

        return Response.status(status).entity(body).build();
    }
}
