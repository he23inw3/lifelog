package jp.he23inw3.asset.adapter.interceptor;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * HTTP リクエストおよびレスポンスの基本情報をログに出力する共通フィルタークラス。
 * <p>
 * すべての API 通信の入出力をフックし、メソッド・パス・HTTP ステータスコードを記録します。
 */
@Slf4j
@Provider
@RequiredArgsConstructor
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Context
    private final ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        log.info(MessageHelper.getMessage("adapter.logging.request", getApiId(), requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri().getPath()));
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        log.info(MessageHelper.getMessage("adapter.logging.response", getApiId(), responseContext.getStatus()));
    }

    private String getApiId() {
        if (resourceInfo != null && resourceInfo.getResourceMethod() != null) {
            Operation operation = resourceInfo.getResourceMethod().getAnnotation(Operation.class);
            if (StringUtils.isNotEmpty(operation.operationId())) {
                return operation.operationId();
            }
        }
        return "UNKNOWN";
    }
}
