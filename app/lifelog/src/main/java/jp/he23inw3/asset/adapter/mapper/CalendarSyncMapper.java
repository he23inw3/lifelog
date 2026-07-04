package jp.he23inw3.asset.adapter.mapper;

import jp.he23inw3.asset.adapter.dto.CalendarSyncResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * カレンダー同期結果を DTO に変換するマッパー。
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface CalendarSyncMapper {

    /**
     * 成功メッセージからレスポンス DTO を構築します。
     *
     * @param message 成功メッセージ
     * @return {@link CalendarSyncResponse}
     */
    default CalendarSyncResponse toSuccessResponse(String message) {
        return CalendarSyncResponse.builder().message(message).build();
    }

    /**
     * エラーメッセージからレスポンス DTO を構築します。
     *
     * @param error エラーメッセージ
     * @return {@link CalendarSyncResponse}
     */
    default CalendarSyncResponse toErrorResponse(String error) {
        return CalendarSyncResponse.builder().error(error).build();
    }
}
