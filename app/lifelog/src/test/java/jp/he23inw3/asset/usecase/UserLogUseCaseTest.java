package jp.he23inw3.asset.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jp.he23inw3.asset.domain.exception.DailyLogValidationException;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.gateway.GeminiGateway;
import jp.he23inw3.asset.domain.gateway.GoogleCalendarGateway;
import jp.he23inw3.asset.domain.model.DayStatus;
import jp.he23inw3.asset.domain.model.GeminiParseResult;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.model.Sentiment;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.DailyLogRepository;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import jp.he23inw3.asset.domain.repository.dto.DailyLogSearchQuery;
import jp.he23inw3.asset.domain.service.DailyLogDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserLogUseCaseTest {

    @Mock
    DailyLogRepository dailyLogRepository;

    @Mock
    UserSettingRepository userSettingRepository;

    @Mock
    GeminiGateway geminiGateway;

    @Mock
    GoogleCalendarGateway googleCalendarGateway;

    @Mock
    DailyLogDomainService dailyLogDomainService;

    @InjectMocks
    UserLogUseCase target;

    @Nested
    @DisplayName("日報ログの解析と登録")
    class CreateLog {

        @Test
        @DisplayName("入力が十分な平日の場合、正常に登録できること")
        void createLog_Success_Weekday() {
            String slackUserId = "U123456";
            String rawText = "今日は8時間設計作業をしました。";
            boolean holiday = false;

            UserSetting setting = UserSetting.builder().slackUserId("U123456").googleCalendarId("test-cal").build();

            GeminiParseResult result = GeminiParseResult.builder().logRelated(true).logDate("2026-06-21")
                    .holiday(false)
                    .workHours(8.0).tasks("設計作業").sentiment(Sentiment.HAPPY).build();

            Log expectedLog = Log.builder().slackUserId("U123456").logDate(LocalDate.of(2026, 6, 21))
                    .rawText("今日は8時間設計作業をしました。").holiday(false).tasks("設計作業").workHours(8.0)
                    .sentiment(Sentiment.HAPPY).build();

            when(userSettingRepository.findById("U123456")).thenReturn(Optional.of(setting));
            when(dailyLogDomainService.determineHolidayStatus(any(LocalDate.class), eq(false), eq("test-cal")))
                    .thenReturn(false);
            when(dailyLogDomainService.isInputInsufficient(any(GeminiParseResult.class), eq(false)))
                    .thenReturn(false);
            when(geminiGateway.parse(eq(rawText), any(LocalDateTime.class), eq("平日")))
                    .thenReturn(result);
            when(dailyLogDomainService.saveDailyLog(eq("U123456"), eq(LocalDate.of(2026, 6, 21)),
                    eq(rawText), any(GeminiParseResult.class), eq("test-cal")))
                    .thenReturn(expectedLog);

            Log log = target.createLog(slackUserId, rawText, holiday);

            assertThat(log).isNotNull();
            assertThat(log.getSlackUserId()).isEqualTo("U123456");
            assertThat(log.getWorkHours()).isEqualTo(8.0);
            assertThat(log.getTasks()).isEqualTo("設計作業");
            assertThat(log.isHoliday()).isFalse();
        }

        @Test
        @DisplayName("日報に関係のない入力の場合、ValidationExceptionが発生すること")
        void createLog_Validation_NotLogRelated() {
            String slackUserId = "U123456";
            String rawText = "こんにちは";
            boolean holiday = false;

            UserSetting setting = UserSetting.builder().slackUserId("U123456").googleCalendarId("test-cal").build();

            GeminiParseResult result = GeminiParseResult.builder().logRelated(false).build();

            when(userSettingRepository.findById("U123456")).thenReturn(Optional.of(setting));
            when(dailyLogDomainService.determineHolidayStatus(any(LocalDate.class), eq(false), eq("test-cal")))
                    .thenReturn(false);
            when(geminiGateway.parse(eq(rawText), any(LocalDateTime.class), eq("平日")))
                    .thenReturn(result);

            assertThatThrownBy(() -> target.createLog(slackUserId, rawText, holiday)).isInstanceOf(DailyLogValidationException.class)
                    .hasMessageContaining("日報に関係のない入力です。");
        }

        @Test
        @DisplayName("平日の勤務時間が不足している場合、ValidationExceptionが発生すること")
        void createLog_Validation_MissingWorkHours() {
            String slackUserId = "U123456";
            String rawText = "設計作業をしました。";
            boolean holiday = false;

            UserSetting setting = UserSetting.builder().slackUserId("U123456").googleCalendarId("test-cal").build();

            GeminiParseResult result = GeminiParseResult.builder().logRelated(true).logDate("2026-06-21")
                    .holiday(false)
                    .workHours(0.0).tasks("設計作業").build();

            when(userSettingRepository.findById("U123456")).thenReturn(Optional.of(setting));
            when(dailyLogDomainService.determineHolidayStatus(any(LocalDate.class), eq(false), eq("test-cal")))
                    .thenReturn(false);
            when(dailyLogDomainService.isInputInsufficient(any(GeminiParseResult.class), eq(false)))
                    .thenReturn(true);
            when(dailyLogDomainService.buildMissingFieldsMessage(any(GeminiParseResult.class), eq(false)))
                    .thenReturn("稼働時間");
            when(geminiGateway.parse(eq(rawText), any(LocalDateTime.class), eq("平日")))
                    .thenReturn(result);

            assertThatThrownBy(() -> target.createLog(slackUserId, rawText, holiday)).isInstanceOf(DailyLogValidationException.class)
                    .hasMessageContaining("稼働時間");
        }

        @Test
        @DisplayName("平日の作業内容が不足している場合、ValidationExceptionが発生すること")
        void createLog_Validation_MissingTasks() {
            String slackUserId = "U123456";
            String rawText = "8時間働きました。";
            boolean holiday = false;

            UserSetting setting = UserSetting.builder().slackUserId("U123456").googleCalendarId("test-cal").build();

            GeminiParseResult result = GeminiParseResult.builder().logRelated(true).logDate("2026-06-21")
                    .holiday(false)
                    .workHours(8.0).tasks("").build();

            when(userSettingRepository.findById("U123456")).thenReturn(Optional.of(setting));
            when(dailyLogDomainService.determineHolidayStatus(any(LocalDate.class), eq(false), eq("test-cal")))
                    .thenReturn(false);
            when(dailyLogDomainService.isInputInsufficient(any(GeminiParseResult.class), eq(false)))
                    .thenReturn(true);
            when(dailyLogDomainService.buildMissingFieldsMessage(any(GeminiParseResult.class), eq(false)))
                    .thenReturn("作業内容を入力してください。");
            when(geminiGateway.parse(eq(rawText), any(LocalDateTime.class), eq("平日")))
                    .thenReturn(result);

            assertThatThrownBy(() -> target.createLog(slackUserId, rawText, holiday)).isInstanceOf(DailyLogValidationException.class)
                    .hasMessageContaining("作業内容を入力してください。");
        }

        @Test
        @DisplayName("休日の場合は、勤務時間・作業内容が空でも正常に登録できること")
        void createLog_Success_Holiday() {
            String slackUserId = "U123456";
            String rawText = "今日は休みでした。";
            boolean holiday = true;

            UserSetting setting = UserSetting.builder().slackUserId("U123456").googleCalendarId("test-cal").build();

            GeminiParseResult result = GeminiParseResult.builder().logRelated(true).logDate("2026-06-21")
                    .holiday(true)
                    .workHours(0.0).tasks("").sentiment(Sentiment.NEUTRAL).build();

            Log expectedLog = Log.builder().slackUserId("U123456").logDate(LocalDate.of(2026, 6, 21))
                    .rawText("今日は休みでした。").holiday(true).tasks("").workHours(0.0)
                    .sentiment(Sentiment.NEUTRAL).build();

            when(userSettingRepository.findById("U123456")).thenReturn(Optional.of(setting));
            when(dailyLogDomainService.determineHolidayStatus(any(LocalDate.class), eq(true), eq("test-cal")))
                    .thenReturn(true);
            when(dailyLogDomainService.isInputInsufficient(any(GeminiParseResult.class), eq(true)))
                    .thenReturn(false);
            when(geminiGateway.parse(eq(rawText), any(LocalDateTime.class),
                    eq(DayStatus.HOLIDAY.getValue()))).thenReturn(result);
            when(dailyLogDomainService.saveDailyLog(eq("U123456"), eq(LocalDate.of(2026, 6, 21)),
                    eq(rawText), any(GeminiParseResult.class), eq("test-cal")))
                    .thenReturn(expectedLog);

            Log log = target.createLog(slackUserId, rawText, holiday);

            assertThat(log).isNotNull();
            assertThat(log.getSlackUserId()).isEqualTo("U123456");
            assertThat(log.getWorkHours()).isEqualTo(0.0);
            assertThat(log.getTasks()).isEqualTo("");
            assertThat(log.isHoliday()).isTrue();
        }
    }

    @Nested
    @DisplayName("日報ログの未保存解析")
    class AnalyzeLog {

        @Test
        @DisplayName("日報ログを保存せずに正常に解析できること")
        void analyzeLog_Success() {
            String slackUserId = "U123456";
            String rawText = "今日は8時間設計作業をしました。";
            boolean holiday = false;

            UserSetting setting = UserSetting.builder().slackUserId("U123456").googleCalendarId("test-cal").build();

            GeminiParseResult result = GeminiParseResult.builder().logRelated(true).logDate("2026-06-21")
                    .holiday(false)
                    .workHours(8.0).tasks("設計作業").sentiment(Sentiment.HAPPY).build();

            when(userSettingRepository.findById("U123456")).thenReturn(Optional.of(setting));
            when(dailyLogDomainService.determineHolidayStatus(any(LocalDate.class), eq(false), eq("test-cal")))
                    .thenReturn(false);
            when(dailyLogDomainService.isInputInsufficient(any(GeminiParseResult.class), eq(false)))
                    .thenReturn(false);
            when(geminiGateway.parse(eq(rawText), any(LocalDateTime.class), eq("平日")))
                    .thenReturn(result);

            Log log = target.analyzeLog(slackUserId, rawText, holiday);

            assertThat(log).isNotNull();
            assertThat(log.getSlackUserId()).isEqualTo("U123456");
            assertThat(log.getWorkHours()).isEqualTo(8.0);
            assertThat(log.getTasks()).isEqualTo("設計作業");
            assertThat(log.isHoliday()).isFalse();
            // Verify that dailyLogDomainService.saveDailyLog is never called
            verify(dailyLogDomainService, never()).saveDailyLog(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("日報に関係のないテキストの場合、ValidationExceptionが発生すること")
        void analyzeLog_Validation_NotLogRelated() {
            String slackUserId = "U123456";
            String rawText = "雑談";
            boolean holiday = false;

            UserSetting setting = UserSetting.builder().slackUserId("U123456").googleCalendarId("test-cal").build();
            GeminiParseResult result = GeminiParseResult.builder().logRelated(false).build();

            when(userSettingRepository.findById("U123456")).thenReturn(Optional.of(setting));
            when(dailyLogDomainService.determineHolidayStatus(any(LocalDate.class), eq(false), eq("test-cal")))
                    .thenReturn(false);
            when(geminiGateway.parse(eq(rawText), any(LocalDateTime.class), eq("平日")))
                    .thenReturn(result);

            assertThatThrownBy(() -> target.analyzeLog(slackUserId, rawText, holiday)).isInstanceOf(DailyLogValidationException.class);
        }
    }

    @Nested
    @DisplayName("日報ログ一覧の取得")
    class GetLogs {

        @Test
        @DisplayName("期間指定に対応する自分の日報ログ一覧を正常に取得できること")
        void getLogs_Success() {
            DailyLogSearchQuery query = DailyLogSearchQuery.builder()
                    .slackUserId("U123456")
                    .start(LocalDate.of(2026, 6, 1))
                    .end(LocalDate.of(2026, 6, 30))
                    .build();

            Log mockLog = Log.builder().slackUserId("U123456").logDate(LocalDate.of(2026, 6, 15)).build();
            when(dailyLogRepository.findByUserIdAndPeriod(any(DailyLogSearchQuery.class)))
                    .thenReturn(List.of(mockLog));

            List<Log> logs = target.getLogs(query);

            assertThat(logs).hasSize(1);
            verify(dailyLogRepository).findByUserIdAndPeriod(argThat(q -> "U123456".equals(q.getSlackUserId())
                    && LocalDate.of(2026, 6, 1).equals(q.getStart())
                    && LocalDate.of(2026, 6, 30).equals(q.getEnd())));
        }
    }

    @Nested
    @DisplayName("特定の日報ログの取得")
    class GetLog {

        @Test
        @DisplayName("指定ユーザーと日付に紐づく日報を正常に取得できること")
        void getLog_Success() {
            String slackUserId = "U123456";
            LocalDate date = LocalDate.of(2026, 6, 21);
            Log mockLog = Log.builder().slackUserId(slackUserId).logDate(date).build();

            when(dailyLogRepository.findByUserIdAndDate(slackUserId, date)).thenReturn(Optional.of(mockLog));

            Log log = target.getLog(slackUserId, date);

            assertThat(log).isNotNull();
            assertThat(log.getSlackUserId()).isEqualTo(slackUserId);
            assertThat(log.getLogDate()).isEqualTo(date);
        }

        @Test
        @DisplayName("指定日付の日報が存在しない場合にResourceNotFoundExceptionをスローすること")
        void getLog_NotFound_ThrowsResourceNotFoundException() {
            String slackUserId = "U123456";
            LocalDate date = LocalDate.of(2026, 6, 21);

            when(dailyLogRepository.findByUserIdAndDate(slackUserId, date)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> target.getLog(slackUserId, date))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Daily log not found for " + slackUserId + " on " + date);
        }
    }

    @Nested
    @DisplayName("カレンダーへの再同期")
    class SyncCalendar {

        @Test
        @DisplayName("指定日の日報をGoogleカレンダーの予定へ正常に再同期できること")
        void syncCalendar_Success() {
            String slackUserId = "U123456";
            LocalDate date = LocalDate.of(2026, 6, 21);
            UserSetting setting = UserSetting.builder().slackUserId(slackUserId).googleCalendarId("cal-123").build();
            Log mockLog = Log.builder().slackUserId(slackUserId).logDate(date).workHours(8.0).tasks("設計").build();

            when(userSettingRepository.findById(slackUserId)).thenReturn(Optional.of(setting));
            when(dailyLogRepository.findByUserIdAndDate(slackUserId, date)).thenReturn(Optional.of(mockLog));
            when(dailyLogDomainService.buildCalendarDescription(any(), any(), any())).thenReturn("Desc");

            target.syncCalendar(slackUserId, date);

            verify(googleCalendarGateway).insertOrUpdateEvent(eq("cal-123"), eq(date), contains("8.0"), eq("Desc"));
        }

        @Test
        @DisplayName("ユーザー設定が存在しない場合にResourceNotFoundExceptionをスローすること")
        void syncCalendar_UserSettingNotFound_ThrowsResourceNotFoundException() {
            String slackUserId = "U123456";
            LocalDate date = LocalDate.of(2026, 6, 21);

            when(userSettingRepository.findById(slackUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> target.syncCalendar(slackUserId, date))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User settings not found for " + slackUserId);
        }

        @Test
        @DisplayName("該当日付の日報が存在しない場合にResourceNotFoundExceptionをスローすること")
        void syncCalendar_DailyLogNotFound_ThrowsResourceNotFoundException() {
            String slackUserId = "U123456";
            LocalDate date = LocalDate.of(2026, 6, 21);
            UserSetting setting = UserSetting.builder().slackUserId(slackUserId).googleCalendarId("cal-123").build();

            when(userSettingRepository.findById(slackUserId)).thenReturn(Optional.of(setting));
            when(dailyLogRepository.findByUserIdAndDate(slackUserId, date)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> target.syncCalendar(slackUserId, date))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Daily log not found for " + slackUserId + " on " + date);
        }
    }
}
