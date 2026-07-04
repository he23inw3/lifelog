package jp.he23inw3.asset.adapter.rest;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.List;
import jp.he23inw3.asset.adapter.constant.ApiPath;
import jp.he23inw3.asset.adapter.constant.ApiTag;
import jp.he23inw3.asset.adapter.dto.LogCalendarSyncResponse;
import jp.he23inw3.asset.adapter.dto.LogDetailResponse;
import jp.he23inw3.asset.adapter.dto.LogListResponse;
import jp.he23inw3.asset.adapter.dto.LogSearchQueryRequest;
import jp.he23inw3.asset.adapter.mapper.LogCalendarSyncMapper;
import jp.he23inw3.asset.adapter.mapper.LogMapper;
import jp.he23inw3.asset.adapter.util.DateParser;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.model.Sentiment;
import jp.he23inw3.asset.usecase.AdminLogUseCase;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * 日報ログ管理エンドポイントを提供するREST リソースクラス
 */
@Path(ApiPath.ADMIN_LOGS)
@Tag(name = ApiTag.ADMIN)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class LogResource {

    private final AdminLogUseCase adminLogUseCase;

    private final LogMapper logMapper;

    private final LogCalendarSyncMapper logCalendarSyncMapper;

    /**
     * 日報ログを検索します
     * 
     * @param request 検索条件リクエストDTO
     * @return 検索条件に合致する日報ログのリスト
     */
    @GET
    @Operation(operationId = "BE-API406", summary = "日報検索", description = "指定された検索条件に対応する日報ログ一覧を取得します")
    public LogListResponse searchLogs(@BeanParam LogSearchQueryRequest request) {

        LocalDate fromDate = StringUtils.isNotBlank(request.getFrom()) ? DateParser.parseDate(request.getFrom()) : null;
        LocalDate toDate = StringUtils.isNotBlank(request.getTo()) ? DateParser.parseDate(request.getTo()) : null;
        Sentiment sentiment = StringUtils.isNotBlank(request.getSentiment()) ? Sentiment.fromValue(request.getSentiment()) : null;

        List<Log> logs = adminLogUseCase.searchLogs(request.getUser(), fromDate, toDate, request.getHoliday(), sentiment);
        List<LogListResponse.Log> responseLogs = logMapper.toListResponseLogList(logs);
        return LogListResponse.builder()
                .totalSize(CollectionUtils.size(responseLogs))
                .logs(responseLogs)
                .build();
    }

    /**
     * 日報ログ詳細を取得します
     * 
     * @param slackUserId Slack ユーザーID
     * @param logDateStr ログ日付文字列 (YYYY-MM-DD形式)
     * @return 日報ログ詳細レスポンス DTO
     */
    @GET
    @Path("/{slackUserId}/{logDate}")
    @Operation(operationId = "BE-API407", summary = "日報詳細取得", description = "指定されたユーザーIDと日付に対応する日報ログ詳細を取得します")
    public LogDetailResponse getLogDetail(@PathParam("slackUserId") String slackUserId,
            @PathParam("logDate") String logDateStr) {
        Log log = adminLogUseCase.getLogDetail(slackUserId, DateParser.parseDate(logDateStr));
        return logMapper.toDetailResponse(log);
    }

    /**
     * 日報から Google Calendar の再同期を行います
     * 
     * @param slackUserId Slack ユーザーID
     * @param logDateStr ログ日付文字列 (YYYY-MM-DD形式)
     * @return HTTP レスポンス
     */
    @POST
    @Path("/{slackUserId}/{logDate}/calendar-sync")
    @Operation(operationId = "BE-API408", summary = "Google Calendar再同期", description = "日報の登録内容をGoogleカレンダーの予定に再同期します")
    public LogCalendarSyncResponse syncCalendar(@PathParam("slackUserId") String slackUserId,
            @PathParam("logDate") String logDateStr) {
        adminLogUseCase.syncCalendar(slackUserId, DateParser.parseDate(logDateStr));
        return logCalendarSyncMapper.toResponse("Calendar synced successfully.");
    }
}
