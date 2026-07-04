package jp.he23inw3.asset.adapter.mapper;

import jp.he23inw3.asset.adapter.dto.HealthResponse;
import jp.he23inw3.asset.domain.model.HealthCheckResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * {@link HealthCheckResult} ユースケース DTO → {@link HealthResponse} adapter DTO 変換マッパー。
 * <p>
 * {@code status} と {@code components} は同名フィールドなので自動マッピングされる。<br>
 * {@code application} と {@code version} は Quarkus の {@code @ConfigProperty} 由来のため、 呼び出し元から引数として渡す。
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface HealthMapper {

    /**
     * {@link HealthCheckResult} を {@link HealthResponse} に変換する。
     *
     * @param result ヘルスチェック結果（{@code status}, {@code components} を引き継ぐ）
     * @param applicationName アプリケーション名（quarkus.application.name）
     * @param applicationVersion アプリケーションバージョン（quarkus.application.version）
     * @return {@link HealthResponse}
     */
    @Mapping(target = "application", source = "applicationName")
    @Mapping(target = "version", source = "applicationVersion")
    HealthResponse toResponse(HealthCheckResult result, String applicationName, String applicationVersion);
}
