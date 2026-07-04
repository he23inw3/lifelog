package jp.he23inw3.asset.adapter.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jp.he23inw3.asset.adapter.constant.ApiPath;
import jp.he23inw3.asset.adapter.constant.ApiTag;
import jp.he23inw3.asset.adapter.dto.HealthResponse;
import jp.he23inw3.asset.adapter.mapper.HealthMapper;
import jp.he23inw3.asset.domain.model.HealthStatus;
import jp.he23inw3.asset.usecase.HealthUseCase;
import jp.he23inw3.asset.domain.model.HealthCheckResult;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Cloud Run の Liveness / Readiness プローブ用ヘルスチェックハンドラー。
 */
@Path(ApiPath.HEALTH)
@Tag(name = ApiTag.SYSTEM)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class HealthResource {

    private final HealthUseCase healthUseCase;

    private final HealthMapper healthMapper;

    @ConfigProperty(name = "quarkus.application.name", defaultValue = "lifelog")
    private final String applicationName;

    @ConfigProperty(name = "quarkus.application.version", defaultValue = "unknown")
    private final String applicationVersion;

    /**
     * ヘルスチェックエンドポイント。
     *
     * @return システム状態（UP / DOWN）
     */
    @GET
    @Operation(operationId = "BE-API000", summary = "ヘルスチェック", description = "Cloud Run のプローブ用ヘルスチェックエンドポイント")
    public Response health() {
        HealthCheckResult result = healthUseCase.check();
        HealthResponse response = healthMapper.toResponse(result, applicationName, applicationVersion);

        return HealthStatus.UP == result.getStatus()
                ? Response.ok(response).build()
                : Response.status(Response.Status.SERVICE_UNAVAILABLE.getStatusCode())
                        .entity(response)
                        .build();
    }
}
