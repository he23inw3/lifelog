package jp.he23inw3.asset.adapter.mapper;

import jp.he23inw3.asset.adapter.dto.SlackLinkageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Slack アカウント連携結果を DTO に変換するマッパー。
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface SlackLinkageMapper {

    /**
     * メッセージ文字列からレスポンス DTO を構築します。
     *
     * @param message 結果メッセージ
     * @return {@link SlackLinkageResponse}
     */
    default SlackLinkageResponse toResponse(String message) {
        return SlackLinkageResponse.builder().message(message).build();
    }
}
