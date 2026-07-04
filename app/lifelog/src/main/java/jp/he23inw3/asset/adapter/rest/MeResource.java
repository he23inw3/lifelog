package jp.he23inw3.asset.adapter.rest;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import jp.he23inw3.asset.adapter.constant.ApiPath;
import jp.he23inw3.asset.adapter.constant.ApiTag;
import jp.he23inw3.asset.adapter.context.ApiContext;
import jp.he23inw3.asset.adapter.dto.CalendarSyncResponse;
import jp.he23inw3.asset.adapter.dto.LogCreateRequest;
import jp.he23inw3.asset.adapter.dto.LogDetailResponse;
import jp.he23inw3.asset.adapter.dto.LogListResponse;
import jp.he23inw3.asset.adapter.dto.MyDashboardResponse;
import jp.he23inw3.asset.adapter.dto.SlackLinkageRequest;
import jp.he23inw3.asset.adapter.dto.SlackLinkageResponse;
import jp.he23inw3.asset.adapter.dto.UserIntegrationsResponse;
import jp.he23inw3.asset.adapter.dto.UserSettingRequest;
import jp.he23inw3.asset.adapter.dto.UserSettingResponse;
import jp.he23inw3.asset.adapter.mapper.CalendarSyncMapper;
import jp.he23inw3.asset.adapter.mapper.DashboardMapper;
import jp.he23inw3.asset.adapter.mapper.LogMapper;
import jp.he23inw3.asset.adapter.mapper.SlackLinkageMapper;
import jp.he23inw3.asset.adapter.mapper.UserSettingMapper;
import jp.he23inw3.asset.adapter.util.DateParser;
import jp.he23inw3.asset.domain.exception.InvalidRequestException;
import jp.he23inw3.asset.domain.exception.LifeLogException;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import jp.he23inw3.asset.usecase.SlackLinkageUseCase;
import jp.he23inw3.asset.usecase.UserDashboardUseCase;
import jp.he23inw3.asset.usecase.UserLogUseCase;
import jp.he23inw3.asset.usecase.UserSettingUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * ログインユーザー自身の情報・設定・日報・ダッシュボードに関するエンドポイントを提供する REST リソースクラス。
 * <p>
 * OIDC 認証済みのユーザーが自分自身のデータを操作します。 デモモードではデモユーザーのメールアドレスが {@link ApiContext} にセットされます。
 */
@Slf4j
@Path(ApiPath.USERS_ME)
@Tag(name = ApiTag.USER)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class MeResource {

    private final ApiContext apiContext;

    private final UserSettingUseCase userSettingUseCase;

    private final UserSettingMapper userSettingMapper;

    private final UserLogUseCase userLogUseCase;

    private final LogMapper logMapper;

    private final UserDashboardUseCase userDashboardUseCase;

    private final SlackLinkageUseCase slackLinkageUseCase;

    private final DashboardMapper dashboardMapper;

    private final SlackLinkageMapper slackLinkageMapper;

    private final CalendarSyncMapper calendarSyncMapper;

    // =========================================================================
    // 利用者情報取得 (BE-API101)
    // =========================================================================

    /**
     * 現在ログイン中の利用者情報を取得します。
     *
     * @return ログイン中の利用者設定情報レスポンス DTO
     */
    @GET
    @Operation(operationId = "BE-API101", summary = "利用者情報取得", description = "現在ログイン中の利用者の設定情報を取得します")
    public UserSettingResponse getMe() {
        String email = apiContext.getAuthenticatedUserId();
        UserSetting setting = userSettingUseCase.getSettingByEmail(email);
        return userSettingMapper.toResponse(setting);
    }

    // =========================================================================
    // 個人設定更新 (BE-API103 / BE-API104)
    // =========================================================================

    /**
     * 個人設定情報を取得します。
     *
     * @return 登録済みの個人設定情報レスポンス DTO
     */
    @GET
    @Path("/settings")
    @Operation(operationId = "BE-API103", summary = "個人設定情報取得", description = "登録されている個人設定情報（日報入力催促時刻など）を取得します")
    public UserSettingResponse getSettings() {
        String email = apiContext.getAuthenticatedUserId();
        UserSetting setting = userSettingUseCase.getSettingByEmail(email);
        return userSettingMapper.toResponse(setting);
    }

    /**
     * 個人設定情報を更新します。
     *
     * @param request 設定更新要求 DTO
     * @return 更新後の個人設定情報レスポンス DTO
     */
    @PUT
    @Path("/settings")
    @Operation(operationId = "BE-API104", summary = "個人設定情報更新", description = "個人設定情報（日報入力催促時刻など）を更新します")
    public UserSettingResponse updateSettings(@Valid UserSettingRequest request) {
        String email = apiContext.getAuthenticatedUserId();
        UserSetting existing = userSettingUseCase.getSettingByEmail(email);
        UserSetting setting = userSettingMapper.toDomain(request, existing.getSlackUserId()).toBuilder()
                .email(email)
                .build();
        UserSetting saved = userSettingUseCase.saveSetting(setting);
        return userSettingMapper.toResponse(saved);
    }

    // =========================================================================
    // 日報 (BE-API105 / BE-API106 / BE-API107 / BE-API108)
    // =========================================================================

    /**
     * 日報・日記を登録します。
     * <p>
     * Gemini で解析し、Calendar（デモ or 本番）への同期、Firestore/BigQuery 保存を行います。
     *
     * @param request 日報登録リクエスト DTO
     * @return HTTP 201 Created と登録後の日報レスポンス DTO
     */
    @POST
    @Path("/logs")
    @Operation(operationId = "BE-API105", summary = "日報登録", description = "日報・日記を登録します。Gemini 解析・Calendar 同期を含みます。")
    public Response createLog(@Valid LogCreateRequest request) {
        String email = apiContext.getAuthenticatedUserId();
        UserSetting setting = userSettingUseCase.getSettingByEmail(email);

        userLogUseCase.createLog(setting.getSlackUserId(), request.getRawText(), request.isHoliday());
        return Response.status(Response.Status.CREATED).build();
    }

    /**
     * 日報の事前解析を行います（保存・カレンダー同期は行いません）。
     *
     * @param request 日報解析要求 DTO
     * @return 解析済みの仮日報ログレスポンス DTO
     */
    @POST
    @Path("/logs/analyze")
    @Operation(operationId = "BE-API108", summary = "日報解析", description = "日報・日記を解析します。Gemini 解析とバリデーションを含みますが、登録・同期は行いません。")
    public LogDetailResponse analyzeLog(@Valid LogCreateRequest request) {
        String email = apiContext.getAuthenticatedUserId();
        UserSetting setting = userSettingUseCase.getSettingByEmail(email);
        Log log = userLogUseCase.analyzeLog(setting.getSlackUserId(), request.getRawText(), request.isHoliday());
        return logMapper.toDetailResponse(log);
    }

    // =========================================================================
    // 自己日報検索 (BE-API106 / BE-API107)
    // =========================================================================

    /**
     * 条件に一致する自分の日報一覧を取得します。
     *
     * @param fromStr 検索開始日 (YYYY-MM-DD、省略可)
     * @param toStr 検索終了日 (YYYY-MM-DD、省略可)
     * @return 日報レスポンス DTO のリスト
     */
    @GET
    @Path("/logs")
    @Operation(operationId = "BE-API106", summary = "日報一覧取得", description = "指定条件に一致する自分の日報一覧を取得します")
    public LogListResponse getLogs(@QueryParam("from") String fromStr, @QueryParam("to") String toStr) {
        String email = apiContext.getAuthenticatedUserId();
        UserSetting setting = userSettingUseCase.getSettingByEmail(email);
        LocalDate today = DateTimeUtil.nowLocalDate();
        LocalDate from = StringUtils.isNotBlank(fromStr) ? DateParser.parseDate(fromStr) : today.withDayOfMonth(1);
        LocalDate to = StringUtils.isNotBlank(toStr) ? DateParser.parseDate(toStr)
                : today.withDayOfMonth(today.lengthOfMonth());

        if (from.isAfter(to)) {
            throw new InvalidRequestException("開始日は終了日以前の日付を指定してください。");
        }
        if (ChronoUnit.DAYS.between(from, to) >= 31) {
            throw new InvalidRequestException("日報の検索範囲は最大で31日間までです。");
        }

        List<Log> logs = userLogUseCase.getLogs(logMapper.toDailySearchQuery(setting.getSlackUserId(), from, to));
        List<LogListResponse.Log> responseLogs = logMapper.toListResponseLogList(logs);
        return LogListResponse.builder()
                .totalSize(CollectionUtils.size(responseLogs))
                .logs(responseLogs)
                .build();
    }

    /**
     * 指定日の日報詳細を取得します。
     *
     * @param logDateStr 対象日付 (YYYY-MM-DD)
     * @return 日報レスポンス DTO
     */
    @GET
    @Path("/logs/{logDate}")
    @Operation(operationId = "BE-API107", summary = "日報詳細取得", description = "指定日の日報詳細を取得します")
    public LogDetailResponse getLog(@PathParam("logDate") String logDateStr) {
        String email = apiContext.getAuthenticatedUserId();
        UserSetting setting = userSettingUseCase.getSettingByEmail(email);
        LocalDate logDate = DateParser.parseDate(logDateStr);
        Log log = userLogUseCase.getLog(setting.getSlackUserId(), logDate);
        return logMapper.toDetailResponse(log);
    }

    // =========================================================================
    // マイダッシュボード (BE-API109)
    // =========================================================================

    /**
     * 自分の当月統計情報を取得します。
     *
     * @return マイダッシュボードレスポンス DTO
     */
    @GET
    @Path("/dashboard")
    @Operation(operationId = "BE-API109", summary = "マイダッシュボード取得", description = "自分の当月統計情報（日報登録件数・稼働時間等）を取得します")
    public MyDashboardResponse getMyDashboard() {
        String email = apiContext.getAuthenticatedUserId();
        UserSetting setting = userSettingUseCase.getSettingByEmail(email);
        UserDashboardUseCase.UserDashboardStats stats = userDashboardUseCase.getStats(setting.getSlackUserId());
        return dashboardMapper.toMyDashboardResponse(stats);
    }

    // =========================================================================
    // アカウント連携 (BE-API110 / BE-API111)
    // =========================================================================

    /**
     * Slack アカウントの連携を行います。
     *
     * @param request 連携トークンを含むリクエスト DTO
     * @return 連携成功時の応答
     */
    @POST
    @Path("/link-slack")
    @Operation(operationId = "BE-API110", summary = "Slack連携実行", description = "一時トークンを検証し、現在のログインユーザーに Slack ユーザーIDを紐づけます。")
    public SlackLinkageResponse linkSlack(@Valid SlackLinkageRequest request) {
        String email = apiContext.getAuthenticatedUserId();
        String token = request.getToken();

        slackLinkageUseCase.linkSlack(email, token);
        return slackLinkageMapper.toResponse("Slack account successfully linked.");
    }

    /**
     * 現在のログインユーザーの外部サービス連携状態を取得します。
     *
     * @return 外部サービス連携状態レスポンス DTO
     */
    @GET
    @Path("/integrations")
    @Operation(operationId = "BE-API111", summary = "外部連携状態取得", description = "GoogleカレンダーおよびSlackのアカウント連携状態を取得します。")
    public UserIntegrationsResponse getIntegrations() {
        String email = apiContext.getAuthenticatedUserId();

        try {
            UserSetting setting = userSettingUseCase.getSettingByEmail(email);
            return userSettingMapper.toIntegrationsResponse(setting);
        } catch (ResourceNotFoundException e) {
            // 設定未作成の新規ユーザー
            return userSettingMapper.toIntegrationsResponse(null);
        }
    }

    // =========================================================================
    // Google Calendar 再同期 (BE-API114)
    // =========================================================================

    /**
     * 指定日の日報を Google カレンダーに再同期します。
     *
     * @param logDateStr 対象日付 (YYYY-MM-DD)
     * @return 再同期成功時の応答
     */
    @POST
    @Path("/logs/{logDate}/calendar-sync")
    @Operation(operationId = "BE-API114", summary = "Google Calendar再同期", description = "指定日の日報をGoogleカレンダーの予定に再同期します。")
    public CalendarSyncResponse syncCalendar(@PathParam("logDate") String logDateStr) {
        String email = apiContext.getAuthenticatedUserId();
        UserSetting setting = userSettingUseCase.getSettingByEmail(email);
        LocalDate logDate = DateParser.parseDate(logDateStr);

        try {
            userLogUseCase.syncCalendar(setting.getSlackUserId(), logDate);
            return calendarSyncMapper.toSuccessResponse("Calendar synced successfully.");
        } catch (Exception e) {
            log.error(MessageHelper.getMessage("adapter.rest.calendar.sync.error", email, logDate), e);
            throw new LifeLogException("カレンダーの再同期に失敗しました。Google連携の設定をご確認ください。", e);
        }
    }
}
