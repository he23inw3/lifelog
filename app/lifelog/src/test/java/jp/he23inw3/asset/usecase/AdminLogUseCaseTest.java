package jp.he23inw3.asset.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import jp.he23inw3.asset.domain.constant.UserMessageConstants;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.gateway.GoogleCalendarGateway;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.model.Sentiment;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.DailyLogRepository;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import jp.he23inw3.asset.domain.service.DailyLogDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminLogUseCaseTest {

    @Mock
    DailyLogRepository dailyLogRepository;

    @Mock
    UserSettingRepository userSettingRepository;

    @Mock
    GoogleCalendarGateway googleCalendarGateway;

    @Mock
    DailyLogDomainService dailyLogDomainService;

    @InjectMocks
    AdminLogUseCase target;

    @Nested
    @DisplayName("日報ログ検索のテスト")
    class SearchLogs {

        @Test
        @DisplayName("条件に基づいて日報検索が行われ、その結果が返されること")
        void testSearchLogs() {
            String user = "user1";
            LocalDate from = LocalDate.of(2026, 6, 1);
            LocalDate to = LocalDate.of(2026, 6, 30);
            Boolean holiday = false;
            Sentiment sentiment = Sentiment.HAPPY;

            List<Log> expected = List.of(mock(Log.class));
            when(dailyLogRepository.findByAdminQuery(user, from, to, holiday, sentiment))
                    .thenReturn(expected);

            List<Log> actual = target.searchLogs(user, from, to, holiday, sentiment);

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("日報ログ詳細取得のテスト")
    class GetLogDetail {

        @Test
        @DisplayName("日報が存在する場合は取得できること")
        void testGetLogDetail_Success() {
            String userId = "U12345";
            LocalDate date = LocalDate.of(2026, 6, 30);
            Log expected = mock(Log.class);

            when(dailyLogRepository.findByUserIdAndDate(userId, date)).thenReturn(Optional.of(expected));

            Log actual = target.getLogDetail(userId, date);

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("日報が存在しない場合はResourceNotFoundExceptionが発生すること")
        void testGetLogDetail_NotFound() {
            String userId = "U12345";
            LocalDate date = LocalDate.of(2026, 6, 30);

            when(dailyLogRepository.findByUserIdAndDate(userId, date)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> target.getLogDetail(userId, date))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Daily log not found");
        }
    }

    @Nested
    @DisplayName("カレンダー再同期のテスト")
    class SyncCalendar {

        @Test
        @DisplayName("通常勤務の日報が存在する場合、カレンダーに同期されること")
        void testSyncCalendar_Work_Success() {
            String userId = "U12345";
            LocalDate date = LocalDate.of(2026, 6, 30);

            UserSetting setting = UserSetting.builder().googleCalendarId("cal-abc").build();
            Log logObj = Log.builder()
                    .holiday(false)
                    .workHours(7.5)
                    .tasks("作業内容")
                    .diary("日記")
                    .sentiment(Sentiment.HAPPY)
                    .build();

            when(userSettingRepository.findById(userId)).thenReturn(Optional.of(setting));
            when(dailyLogRepository.findByUserIdAndDate(userId, date)).thenReturn(Optional.of(logObj));
            when(dailyLogDomainService.buildCalendarDescription("作業内容", "日記", "HAPPY"))
                    .thenReturn("説明文");

            target.syncCalendar(userId, date);

            String expectedTitle = UserMessageConstants.WORKFLOW_CALENDAR_TITLE_WORK.replace("{0}", "7.5");
            verify(googleCalendarGateway).insertOrUpdateEvent("cal-abc", date, expectedTitle, "説明文");
        }

        @Test
        @DisplayName("休暇・休日の日報が存在する場合、休暇のタイトルで同期されること")
        void testSyncCalendar_Holiday_Success() {
            String userId = "U12345";
            LocalDate date = LocalDate.of(2026, 6, 30);

            UserSetting setting = UserSetting.builder().googleCalendarId("cal-abc").build();
            Log logObj = Log.builder()
                    .holiday(true)
                    .workHours(0.0)
                    .tasks("")
                    .diary("お休み")
                    .sentiment(Sentiment.NEUTRAL)
                    .build();

            when(userSettingRepository.findById(userId)).thenReturn(Optional.of(setting));
            when(dailyLogRepository.findByUserIdAndDate(userId, date)).thenReturn(Optional.of(logObj));
            when(dailyLogDomainService.buildCalendarDescription("", "お休み", "NEUTRAL"))
                    .thenReturn("お休み説明");

            target.syncCalendar(userId, date);

            verify(googleCalendarGateway).insertOrUpdateEvent("cal-abc", date, UserMessageConstants.WORKFLOW_CALENDAR_TITLE_HOLIDAY, "お休み説明");
        }

        @Test
        @DisplayName("ユーザー設定が存在しない場合はResourceNotFoundExceptionが発生すること")
        void testSyncCalendar_UserNotFound() {
            String userId = "U12345";
            LocalDate date = LocalDate.of(2026, 6, 30);

            when(userSettingRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> target.syncCalendar(userId, date))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User settings not found");
        }

        @Test
        @DisplayName("日報が存在しない場合はResourceNotFoundExceptionが発生すること")
        void testSyncCalendar_LogNotFound() {
            String userId = "U12345";
            LocalDate date = LocalDate.of(2026, 6, 30);
            UserSetting setting = UserSetting.builder().googleCalendarId("cal-abc").build();

            when(userSettingRepository.findById(userId)).thenReturn(Optional.of(setting));
            when(dailyLogRepository.findByUserIdAndDate(userId, date)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> target.syncCalendar(userId, date))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Daily log not found");
        }
    }
}
