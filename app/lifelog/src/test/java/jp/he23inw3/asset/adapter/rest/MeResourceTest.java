package jp.he23inw3.asset.adapter.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;
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
import jp.he23inw3.asset.domain.exception.InvalidRequestException;
import jp.he23inw3.asset.domain.exception.InvalidTokenException;
import jp.he23inw3.asset.domain.exception.LifeLogException;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.usecase.SlackLinkageUseCase;
import jp.he23inw3.asset.usecase.UserDashboardUseCase;
import jp.he23inw3.asset.usecase.UserLogUseCase;
import jp.he23inw3.asset.usecase.UserSettingUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MeResourceTest {

    static final String USER_ID = "U123456";

    @Mock
    ApiContext apiContext;

    @Mock
    UserSettingUseCase userSettingUseCase;

    @Mock
    UserSettingMapper userSettingMapper;

    @Mock
    UserLogUseCase userLogUseCase;

    @Mock
    LogMapper logMapper;

    @Mock
    UserDashboardUseCase userDashboardUseCase;

    @Mock
    SlackLinkageUseCase slackLinkageUseCase;

    @Mock
    DashboardMapper dashboardMapper;

    @Mock
    SlackLinkageMapper slackLinkageMapper;

    @Mock
    CalendarSyncMapper calendarSyncMapper;

    @InjectMocks
    MeResource target;

    // =========================================================================
    // BE-API101 利用者情報取得
    // =========================================================================
    @Nested
    @DisplayName("利用者情報取得")
    class GetMe {

        @Test
        @DisplayName("ログイン中の利用者情報を正常に取得できること")
        void getMe_Success() {
            UserSetting mockSetting = UserSetting.builder()
                    .slackUserId(USER_ID)
                    .email(USER_ID)
                    .build();
            UserSettingResponse mockResponse = UserSettingResponse.builder()
                    .slackUserId(USER_ID)
                    .build();

            when(apiContext.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(userSettingUseCase.getSettingByEmail(USER_ID)).thenReturn(mockSetting);
            when(userSettingMapper.toResponse(mockSetting)).thenReturn(mockResponse);

            UserSettingResponse response = target.getMe();

            assertThat(response.getSlackUserId()).isEqualTo(USER_ID);
            verify(userSettingUseCase).getSettingByEmail(USER_ID);
        }
    }

    // =========================================================================
    // BE-API103 設定取得
    // =========================================================================
    @Nested
    @DisplayName("設定取得")
    class GetSettings {

        @Test
        @DisplayName("自分の設定情報を正常に取得できること")
        void getSettings_Success() {
            UserSetting mockSetting = UserSetting.builder()
                    .slackUserId(USER_ID)
                    .email(USER_ID)
                    .remindTime("18:00")
                    .build();
            UserSettingResponse mockResponse = UserSettingResponse.builder()
                    .slackUserId(USER_ID)
                    .remindTime("18:00")
                    .build();

            when(apiContext.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(userSettingUseCase.getSettingByEmail(USER_ID)).thenReturn(mockSetting);
            when(userSettingMapper.toResponse(mockSetting)).thenReturn(mockResponse);

            UserSettingResponse response = target.getSettings();

            assertThat(response.getRemindTime()).isEqualTo("18:00");
            verify(userSettingUseCase).getSettingByEmail(USER_ID);
        }
    }

    // =========================================================================
    // BE-API104 設定更新
    // =========================================================================
    @Nested
    @DisplayName("設定更新")
    class UpdateSettings {

        @Test
        @DisplayName("自分の設定情報を正常に更新できること")
        void updateSettings_Success() {
            UserSettingRequest request = new UserSettingRequest();
            request.setRemindTime("20:00");
            UserSetting mockSetting = UserSetting.builder()
                    .slackUserId(USER_ID)
                    .email(USER_ID)
                    .build();
            UserSettingResponse mockResponse = UserSettingResponse.builder()
                    .slackUserId(USER_ID)
                    .remindTime("20:00")
                    .build();

            when(apiContext.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(userSettingUseCase.getSettingByEmail(USER_ID)).thenReturn(mockSetting);
            when(userSettingMapper.toDomain(request, USER_ID)).thenReturn(mockSetting);
            when(userSettingUseCase.saveSetting(any(UserSetting.class))).thenReturn(mockSetting);
            when(userSettingMapper.toResponse(mockSetting)).thenReturn(mockResponse);

            UserSettingResponse response = target.updateSettings(request);

            assertThat(response.getRemindTime()).isEqualTo("20:00");
            verify(userSettingUseCase).saveSetting(any(UserSetting.class));
        }
    }

    // =========================================================================
    // BE-API105 日報登録
    // =========================================================================
    @Nested
    @DisplayName("日報登録")
    class CreateLog {

        @Test
        @DisplayName("自分の日報を正常に登録できること")
        void createLog_Success() {
            LogCreateRequest request = new LogCreateRequest();
            request.setRawText("Test Log Content");
            request.setHoliday(false);

            UserSetting mockSetting = UserSetting.builder()
                    .slackUserId(USER_ID)
                    .email(USER_ID)
                    .build();
            Log mockLog = Log.builder()
                    .slackUserId(USER_ID)
                    .build();

            when(apiContext.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(userSettingUseCase.getSettingByEmail(USER_ID)).thenReturn(mockSetting);
            when(userLogUseCase.createLog(USER_ID, request.getRawText(), request.isHoliday())).thenReturn(mockLog);

            Response response = target.createLog(request);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
            verify(userLogUseCase).createLog(USER_ID, request.getRawText(), request.isHoliday());
        }
    }

    // =========================================================================
    // BE-API108 日報解析
    // =========================================================================
    @Nested
    @DisplayName("日報解析")
    class AnalyzeLog {

        @Test
        @DisplayName("自分の日報を正常に解析できること")
        void analyzeLog_Success() {
            LogCreateRequest request = new LogCreateRequest();
            request.setRawText("Test Log Content");
            request.setHoliday(false);

            UserSetting mockSetting = UserSetting.builder()
                    .slackUserId(USER_ID)
                    .email(USER_ID)
                    .build();
            Log mockLog = Log.builder()
                    .slackUserId(USER_ID)
                    .logDate(LocalDate.of(2024, 6, 1))
                    .build();
            LogDetailResponse mockResponse = LogDetailResponse.builder()
                    .slackUserId(USER_ID)
                    .build();

            when(apiContext.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(userSettingUseCase.getSettingByEmail(USER_ID)).thenReturn(mockSetting);
            when(userLogUseCase.analyzeLog(USER_ID, request.getRawText(), request.isHoliday())).thenReturn(mockLog);
            when(logMapper.toDetailResponse(mockLog)).thenReturn(mockResponse);

            LogDetailResponse response = target.analyzeLog(request);

            assertThat(response).isNotNull();
            assertThat(response.getSlackUserId()).isEqualTo(USER_ID);
            verify(userLogUseCase).analyzeLog(USER_ID, request.getRawText(), request.isHoliday());
        }
    }

    // =========================================================================
    // BE-API106 日報一覧取得
    // =========================================================================
    @Nested
    @DisplayName("日報一覧取得")
    class GetLogs {

        @Test
        @DisplayName("自分の日報一覧を正常に取得できること")
        void getLogs_Success() {
            UserSetting mockSetting = UserSetting.builder()
                    .slackUserId(USER_ID)
                    .email(USER_ID)
                    .build();
            Log mockLog = Log.builder()
                    .slackUserId(USER_ID)
                    .logDate(LocalDate.now())
                    .build();
            LogListResponse.Log mockResponse = LogListResponse.Log.builder()
                    .build();
            jp.he23inw3.asset.domain.repository.dto.DailyLogSearchQuery queryDto = jp.he23inw3.asset.domain.repository.dto.DailyLogSearchQuery.builder()
                    .slackUserId(USER_ID)
                    .start(null)
                    .end(null)
                    .build();

            when(apiContext.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(userSettingUseCase.getSettingByEmail(USER_ID)).thenReturn(mockSetting);
            when(logMapper.toDailySearchQuery(eq(USER_ID), any(), any())).thenReturn(queryDto);
            when(userLogUseCase.getLogs(any(jp.he23inw3.asset.domain.repository.dto.DailyLogSearchQuery.class))).thenReturn(List.of(mockLog));
            when(logMapper.toListResponseLogList(List.of(mockLog))).thenReturn(List.of(mockResponse));

            LogListResponse responses = target.getLogs(null, null);

            assertThat(responses.getLogs()).hasSize(1);
            verify(logMapper).toDailySearchQuery(eq(USER_ID), any(), any());
            verify(userLogUseCase)
                    .getLogs(argThat(
                            q -> USER_ID.equals(q.getSlackUserId()) && q.getStart() == null && q.getEnd() == null));
        }

        @Test
        @DisplayName("開始日が終了日より後の場合にInvalidRequestExceptionがスローされること")
        void getLogs_FromAfterTo_ThrowsInvalidRequestException() {
            UserSetting mockSetting = UserSetting.builder()
                    .slackUserId(USER_ID)
                    .email(USER_ID)
                    .build();

            when(apiContext.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(userSettingUseCase.getSettingByEmail(USER_ID)).thenReturn(mockSetting);

            assertThatThrownBy(() -> target.getLogs("2024-06-02", "2024-06-01"))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessage("開始日は終了日以前の日付を指定してください。");
        }

        @Test
        @DisplayName("検索範囲が31日を超える場合にInvalidRequestExceptionがスローされること")
        void getLogs_RangeExceeds31Days_ThrowsInvalidRequestException() {
            UserSetting mockSetting = UserSetting.builder()
                    .slackUserId(USER_ID)
                    .email(USER_ID)
                    .build();

            when(apiContext.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(userSettingUseCase.getSettingByEmail(USER_ID)).thenReturn(mockSetting);

            assertThatThrownBy(() -> target.getLogs("2024-06-01", "2024-07-03"))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessage("日報の検索範囲は最大で31日間までです。");
        }
    }

    // =========================================================================
    // BE-API107 日報詳細取得
    // =========================================================================
    @Nested
    @DisplayName("日報詳細取得")
    class GetLog {

        @Test
        @DisplayName("自分の指定日の日報を正常に取得できること")
        void getLog_Success() {
            UserSetting mockSetting = UserSetting.builder()
                    .slackUserId(USER_ID)
                    .email(USER_ID)
                    .build();
            LocalDate date = LocalDate.of(2024, 6, 1);
            Log mockLog = Log.builder()
                    .slackUserId(USER_ID)
                    .logDate(date)
                    .build();
            LogDetailResponse mockResponse = LogDetailResponse.builder()
                    .slackUserId(USER_ID)
                    .logDate(date)
                    .build();

            when(apiContext.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(userSettingUseCase.getSettingByEmail(USER_ID)).thenReturn(mockSetting);
            when(userLogUseCase.getLog(USER_ID, date)).thenReturn(mockLog);
            when(logMapper.toDetailResponse(mockLog)).thenReturn(mockResponse);

            LogDetailResponse response = target.getLog("2024-06-01");

            assertThat(response.getLogDate()).isEqualTo(date);
        }

        @Test
        @DisplayName("不正な日付フォーマットでBadRequestエラーとなること")
        void getLog_InvalidDate_BadRequest() {
            UserSetting mockSetting = UserSetting.builder()
                    .slackUserId(USER_ID)
                    .email(USER_ID)
                    .build();
            when(apiContext.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(userSettingUseCase.getSettingByEmail(USER_ID)).thenReturn(mockSetting);

            assertThatThrownBy(() -> target.getLog("not-a-date")).isInstanceOf(InvalidRequestException.class);
        }
    }

    // =========================================================================
    // BE-API109 マイダッシュボード取得
    // =========================================================================
    @Nested
    @DisplayName("マイダッシュボード取得")
    class GetMyDashboard {

        @Test
        @DisplayName("自分のダッシュボード統計を正常に取得できること")
        void getMyDashboard_Success() {
            UserSetting mockSetting = UserSetting.builder()
                    .slackUserId(USER_ID)
                    .email(USER_ID)
                    .build();
            UserDashboardUseCase.UserDashboardStats mockStats = new UserDashboardUseCase.UserDashboardStats(
                    5,
                    40.0,
                    2.5,
                    "2024-06-05");
            MyDashboardResponse mockResponse = new MyDashboardResponse();
            mockResponse.setMonthlyLogCount(5);
            mockResponse.setMonthlyWorkHours(40.0);
            mockResponse.setMonthlyOvertimeHours(2.5);
            mockResponse.setLastLogDate("2024-06-05");

            when(apiContext.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(userSettingUseCase.getSettingByEmail(USER_ID)).thenReturn(mockSetting);
            when(userDashboardUseCase.getStats(USER_ID)).thenReturn(mockStats);
            when(dashboardMapper.toMyDashboardResponse(mockStats)).thenReturn(mockResponse);

            MyDashboardResponse response = target.getMyDashboard();

            assertThat(response.getMonthlyLogCount()).isEqualTo(5);
            assertThat(response.getMonthlyWorkHours()).isEqualTo(40.0);
            assertThat(response.getMonthlyOvertimeHours()).isEqualTo(2.5);
            assertThat(response.getLastLogDate()).isEqualTo("2024-06-05");
        }
    }

    // =========================================================================
    // BE-API111 外部連携状態取得
    // =========================================================================
    @Nested
    @DisplayName("外部連携状態取得")
    class GetIntegrations {

        @Test
        @DisplayName("外部連携状態を正常に取得できること")
        void getIntegrations_Success() {
            UserSetting mockSetting = UserSetting.builder()
                    .slackUserId(USER_ID)
                    .email("test@example.com")
                    .googleCalendarId("test-cal")
                    .googleLinked(true)
                    .build();
            UserIntegrationsResponse mockResponse = UserIntegrationsResponse.builder()
                    .googleLinked(true)
                    .googleCalendarId("test-cal")
                    .slackLinked(true)
                    .slackUserId(USER_ID)
                    .build();

            when(apiContext.getAuthenticatedUserId()).thenReturn("test@example.com");
            when(userSettingUseCase.getSettingByEmail("test@example.com")).thenReturn(mockSetting);
            when(userSettingMapper.toIntegrationsResponse(mockSetting)).thenReturn(mockResponse);

            UserIntegrationsResponse response = target.getIntegrations();

            assertThat(response.isGoogleLinked()).isTrue();
            assertThat(response.getGoogleCalendarId()).isEqualTo("test-cal");
            assertThat(response.isSlackLinked()).isTrue();
            assertThat(response.getSlackUserId()).isEqualTo(USER_ID);
        }
    }

    // =========================================================================
    // BE-API110 Slack連携実行
    // =========================================================================
    @Nested
    @DisplayName("Slack連携実行")
    class LinkSlack {

        @Test
        @DisplayName("Slackアカウントの連携実行が正常に処理されること")
        void linkSlack_Success() {
            SlackLinkageRequest request = new SlackLinkageRequest();
            request.setToken("valid-token");
            SlackLinkageResponse mockResponse = SlackLinkageResponse.builder().message("Slack account successfully linked.").build();

            when(apiContext.getAuthenticatedUserId()).thenReturn("test@example.com");
            doNothing().when(slackLinkageUseCase).linkSlack("test@example.com", "valid-token");
            when(slackLinkageMapper.toResponse("Slack account successfully linked.")).thenReturn(mockResponse);

            SlackLinkageResponse response = target.linkSlack(request);

            assertThat(response).isNotNull();
            assertThat(response.getMessage()).isEqualTo("Slack account successfully linked.");
            verify(slackLinkageUseCase).linkSlack("test@example.com", "valid-token");
        }

        @Test
        @DisplayName("無効なトークンによりSlack連携が失敗した場合にInvalidTokenExceptionがスローされること")
        void linkSlack_InvalidToken_ThrowsException() {
            SlackLinkageRequest request = new SlackLinkageRequest();
            request.setToken("invalid-token");

            when(apiContext.getAuthenticatedUserId()).thenReturn("test@example.com");
            doThrow(new InvalidTokenException("Invalid or expired token.")).when(slackLinkageUseCase)
                    .linkSlack("test@example.com", "invalid-token");

            org.junit.jupiter.api.Assertions.assertThrows(
                    InvalidTokenException.class,
                    () -> target.linkSlack(request));
        }
    }

    // =========================================================================
    // BE-API114 Google Calendar 再同期
    // =========================================================================
    @Nested
    @DisplayName("Google Calendar再同期")
    class SyncCalendar {

        @Test
        @DisplayName("Googleカレンダーの予定に日報が正常に再同期されること")
        void syncCalendar_Success() {
            String email = "test@example.com";
            String dateStr = "2024-06-01";
            LocalDate date = LocalDate.of(2024, 6, 1);
            UserSetting mockSetting = UserSetting.builder()
                    .slackUserId(USER_ID)
                    .email(email)
                    .build();
            CalendarSyncResponse mockResponse = CalendarSyncResponse.builder().message("Calendar synced successfully.").build();

            when(apiContext.getAuthenticatedUserId()).thenReturn(email);
            when(userSettingUseCase.getSettingByEmail(email)).thenReturn(mockSetting);
            doNothing().when(userLogUseCase).syncCalendar(USER_ID, date);
            when(calendarSyncMapper.toSuccessResponse("Calendar synced successfully.")).thenReturn(mockResponse);

            CalendarSyncResponse response = target.syncCalendar(dateStr);

            assertThat(response).isNotNull();
            assertThat(response.getMessage()).isEqualTo("Calendar synced successfully.");
            verify(userLogUseCase).syncCalendar(USER_ID, date);
        }

        @Test
        @DisplayName("再同期中にエラーが発生した場合にLifeLogExceptionがスローされること")
        void syncCalendar_Exception_ThrowsLifeLogException() {
            String email = "test@example.com";
            String dateStr = "2024-06-01";
            LocalDate date = LocalDate.of(2024, 6, 1);
            UserSetting mockSetting = UserSetting.builder().slackUserId(USER_ID).email(email).build();

            when(apiContext.getAuthenticatedUserId()).thenReturn(email);
            when(userSettingUseCase.getSettingByEmail(email)).thenReturn(mockSetting);
            doThrow(new RuntimeException("Sync error")).when(userLogUseCase).syncCalendar(USER_ID, date);
            LifeLogException exception = org.junit.jupiter.api.Assertions.assertThrows(
                    LifeLogException.class,
                    () -> target.syncCalendar(dateStr));

            assertThat(exception.getMessage()).isEqualTo("カレンダーの再同期に失敗しました。Google連携の設定をご確認ください。");
        }
    }
}
