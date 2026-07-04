package jp.he23inw3.asset.adapter.mapper;

import java.util.List;
import jp.he23inw3.asset.adapter.dto.DemoCalendarListResponse;
import jp.he23inw3.asset.adapter.dto.DemoMessageListResponse;
import jp.he23inw3.asset.domain.model.DemoCalendarEvent;
import jp.he23inw3.asset.domain.model.DemoMessage;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * デモデータ ↔ adapter 層 DTO の変換マッパー。
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface DemoMapper {

    /**
     * デモカレンダーイベントをレスポンスに変換する。
     * 
     * @param event デモカレンダーイベント
     * @return レスポンス
     */
    DemoCalendarListResponse.CalendarEvent toCalendarEvent(DemoCalendarEvent event);

    /**
     * デモカレンダーイベントのリストをレスポンスに変換する。
     * 
     * @param events デモカレンダーイベントのリスト
     * @return レスポンス
     */
    List<DemoCalendarListResponse.CalendarEvent> toCalendarEventsResponse(List<DemoCalendarEvent> events);

    /**
     * デモメッセージをレスポンスに変換する。
     * 
     * @param message デモメッセージ
     * @return レスポンス
     */
    DemoMessageListResponse.DemoMessage toDemoMessage(DemoMessage message);

    /**
     * デモメッセージのリストをレスポンスに変換する。
     * 
     * @param messages デモメッセージのリスト
     * @return レスポンス
     */
    List<DemoMessageListResponse.DemoMessage> toDemoMessagesResponse(List<DemoMessage> messages);
}
