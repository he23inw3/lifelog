package jp.he23inw3.asset.adapter.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.time.YearMonth;
import java.util.List;
import jp.he23inw3.asset.adapter.dto.DemoCalendarListResponse;
import jp.he23inw3.asset.adapter.dto.DemoMessageListResponse;
import jp.he23inw3.asset.adapter.mapper.DemoMapper;
import jp.he23inw3.asset.domain.exception.InvalidDemoParameterException;
import jp.he23inw3.asset.domain.model.DemoCalendarEvent;
import jp.he23inw3.asset.domain.model.DemoMessage;
import jp.he23inw3.asset.usecase.DemoUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * デモモード用の疑似カレンダーおよび疑似 Slack メッセージ確認用エンドポイントを提供する REST リソースクラス。
 */
@Slf4j
@Path("/api/v1/demo")
@Tag(name = "Demo")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class DemoResource {

    private final DemoUseCase demoUseCase;
    private final DemoMapper demoMapper;

    /**
     * デモ用のカレンダーイベント一覧を取得します。
     *
     * @param monthStr 対象月 (YYYY-MM 形式)
     * @return カレンダーイベントレスポンス DTO
     */
    @GET
    @Path("/calendar")
    @Operation(operationId = "BE-API501", summary = "デモカレンダーイベント一覧取得", description = "デモモードで保存された該当月のカレンダーイベントを取得します")
    public DemoCalendarListResponse getDemoCalendar(@QueryParam("month") String monthStr) {
        if (monthStr == null || !monthStr.matches("^\\d{4}-\\d{2}$")) {
            throw new InvalidDemoParameterException("月フォーマットが正しくありません。YYYY-MM形式で指定してください。");
        }

        YearMonth yearMonth = YearMonth.parse(monthStr);
        List<DemoCalendarEvent> eventList = demoUseCase.getDemoCalendar(yearMonth);
        List<DemoCalendarListResponse.CalendarEvent> responseEvents = demoMapper.toCalendarEventsResponse(eventList);
        return DemoCalendarListResponse.builder()
                .totalSize(CollectionUtils.size(responseEvents))
                .calendarEvents(responseEvents)
                .build();
    }

    /**
     * デモ用の Slack メッセージ一覧を取得します。
     *
     * @param slackUserId 対象 of Slack ユーザー ID（省略時はデモ設定のデフォルト値）
     * @return Slack メッセージレスポンス DTO
     */
    @GET
    @Path("/messages")
    @Operation(operationId = "BE-API502", summary = "デモSlackメッセージ一覧取得", description = "デモモードで保存されたSlack宛メッセージ一覧を取得します")
    public DemoMessageListResponse getDemoMessages(@QueryParam("slackUserId") String slackUserId) {
        List<DemoMessage> messageList = demoUseCase.getDemoMessages(slackUserId);
        List<DemoMessageListResponse.DemoMessage> responseMessages = demoMapper.toDemoMessagesResponse(messageList);
        return DemoMessageListResponse.builder()
                .totalSize(CollectionUtils.size(responseMessages))
                .messages(responseMessages)
                .build();
    }
}
