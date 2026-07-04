package jp.he23inw3.asset.adapter.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import jp.he23inw3.asset.adapter.dto.AdminRequest;
import jp.he23inw3.asset.adapter.dto.AdminResponse;
import jp.he23inw3.asset.domain.model.AdminUser;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * {@link AdminUser} ドメインモデル ↔ adapter 層 DTO の変換マッパー。
 * <p>
 * Quarkus CDI コンテナで管理されるため、{@code @Inject} で注入可能。
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface AdminMapper {

    /**
     * ドメインモデルをレスポンス DTO に変換します。
     *
     * @param admin 管理者のドメインモデル
     * @return 変換後の {@link AdminResponse}
     */
    AdminResponse toResponse(AdminUser admin);

    /**
     * リクエスト DTO とメールアドレスからドメインモデルに変換します。
     *
     * @param request リクエスト DTO
     * @param email 管理者のメールアドレス
     * @return 変換後の {@link AdminUser}
     */
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AdminUser toDomain(AdminRequest request, String email);

    /**
     * Instant を Asia/Tokyo タイムゾーンの LocalDateTime に変換します。
     *
     * @param instant 変換対象の Instant
     * @return 変換後の LocalDateTime
     */
    default LocalDateTime mapInstantToLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, DateTimeUtil.TOKYO_ZONE);
    }
}
