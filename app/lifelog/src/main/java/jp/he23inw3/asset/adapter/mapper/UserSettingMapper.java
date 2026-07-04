package jp.he23inw3.asset.adapter.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import jp.he23inw3.asset.adapter.dto.UserIntegrationsResponse;
import jp.he23inw3.asset.adapter.dto.UserRegistrationRequest;
import jp.he23inw3.asset.adapter.dto.UserSettingRequest;
import jp.he23inw3.asset.adapter.dto.UserSettingResponse;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * {@link UserSetting} ドメインモデル ↔ adapter 層 DTO の変換マッパー。
 * <p>
 * Quarkus CDI コンテナで管理されるため、{@code @Inject} で注入可能。
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface UserSettingMapper {

    /**
     * 外部連携状態のレスポンス DTO に変換します。
     *
     * @param setting ユーザー設定のドメインモデル
     * @return 変換後の {@link UserIntegrationsResponse}
     */
    @Mapping(target = "slackLinked", expression = "java(org.apache.commons.lang3.StringUtils.isNotBlank(setting.getSlackUserId()))")
    UserIntegrationsResponse mapSettingToIntegrations(UserSetting setting);

    /**
     * 外部連携状態のレスポンス DTO に変換します（Nullセーフ）。
     *
     * @param setting ユーザー設定のドメインモデル
     * @return 変換後の {@link UserIntegrationsResponse}
     */
    default UserIntegrationsResponse toIntegrationsResponse(UserSetting setting) {
        if (setting == null) {
            return UserIntegrationsResponse.builder()
                    .googleLinked(false)
                    .googleCalendarId(null)
                    .slackLinked(false)
                    .slackUserId(null)
                    .build();
        }
        return mapSettingToIntegrations(setting);
    }

    /**
     * ドメインモデルをレスポンス DTO に変換します。
     *
     * @param setting ユーザー設定のドメインモデル
     * @return 変換後の {@link UserSettingResponse}
     */
    UserSettingResponse toResponse(UserSetting setting);

    /**
     * ドメインモデルのリストをレスポンス DTO のリストに変換します。
     *
     * @param settings ユーザー設定のリスト
     * @return 変換後の {@link UserSettingResponse} のリスト
     */
    List<UserSettingResponse> toResponseList(List<UserSetting> settings);

    /**
     * リクエスト DTO と Slack ユーザー ID からドメインモデルに変換します。
     *
     * @param request リクエスト DTO
     * @param slackUserId Slack ユーザー ID
     * @return 変換後の {@link UserSetting}
     */
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "googleRefreshToken", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    UserSetting toDomain(UserSettingRequest request, String slackUserId);

    /**
     * 登録リクエスト DTO からドメインモデルに変換します（email なし）。
     *
     * @param request 登録リクエスト DTO
     * @return 変換後の {@link UserSetting}
     */
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "googleRefreshToken", ignore = true)
    UserSetting toDomain(UserRegistrationRequest request);

    /**
     * 登録リクエスト DTO と OIDC メールを組み合わせてドメインモデルに変換します。
     *
     * @param request 登録リクエスト DTO
     * @param email OIDC メールアドレス
     * @return 変換後の {@link UserSetting}
     */
    @Mapping(target = "googleRefreshToken", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    UserSetting toDomain(UserRegistrationRequest request, String email);

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
