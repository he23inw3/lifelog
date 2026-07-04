package jp.he23inw3.asset.adapter.mapper;

import jp.he23inw3.asset.adapter.dto.LogCalendarSyncResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * 管理者向けカレンダー同期結果を DTO に変換するマッパー。
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface LogCalendarSyncMapper {

    /**
     * 成功メッセージからレスポンス DTO を構築します。
     *
     * @param message 成功メッセージ
     * @return {@link LogCalendarSyncResponse}
     */
    default LogCalendarSyncResponse toResponse(String message) {
        return LogCalendarSyncResponse.builder().message(message).build();
    }
}
