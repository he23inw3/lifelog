package jp.he23inw3.asset.usecase;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;
import jp.he23inw3.asset.domain.constant.UserMessageConstants;
import jp.he23inw3.asset.domain.gateway.GoogleCalendarGateway;
import jp.he23inw3.asset.domain.gateway.SlackGateway;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.DailyLogRepository;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RemindCheckUseCaseTest {

    @Mock
    UserSettingRepository userSettingRepository;

    @Mock
    SlackGateway slackGateway;

    @Mock
    GoogleCalendarGateway googleCalendarGateway;

    @Mock
    DailyLogRepository dailyLogRepository;

    @InjectMocks
    RemindCheckUseCase target;

    MockedStatic<LocalDate> mockedLocalDate;
    MockedStatic<LocalTime> mockedLocalTime;

    @BeforeEach
    void setUp() {
        mockedLocalDate = mockStatic(LocalDate.class, CALLS_REAL_METHODS);
        mockedLocalTime = mockStatic(LocalTime.class, CALLS_REAL_METHODS);
    }

    @AfterEach
    void tearDown() {
        mockedLocalDate.close();
        mockedLocalTime.close();
    }

    @Nested
    @DisplayName("リマインド実行判定および送信")
    class CheckAndSendRemind {

        @Test
        @DisplayName("土曜日の場合は処理をスキップすること")
        void checkAndSendRemind_WeekendSaturday_ShouldSkip() {
            // Arrange (2026-06-13 is Saturday)
            LocalDate saturday = LocalDate.of(2026, 6, 13);
            LocalTime dummyTime = LocalTime.of(18, 0);

            mockedLocalDate.when(() -> LocalDate.now(ZoneId.of("Asia/Tokyo"))).thenReturn(saturday);
            mockedLocalTime.when(() -> LocalTime.now(ZoneId.of("Asia/Tokyo"))).thenReturn(dummyTime);

            // Act
            target.checkAndSendRemind();

            // Assert
            verifyNoInteractions(userSettingRepository);
            verifyNoInteractions(slackGateway);
            verifyNoInteractions(googleCalendarGateway);
            verifyNoInteractions(dailyLogRepository);
        }

        @Test
        @DisplayName("日曜日の場合は処理をスキップすること")
        void checkAndSendRemind_WeekendSunday_ShouldSkip() {
            // Arrange (2026-06-14 is Sunday)
            LocalDate sunday = LocalDate.of(2026, 6, 14);
            LocalTime dummyTime = LocalTime.of(18, 0);

            mockedLocalDate.when(() -> LocalDate.now(ZoneId.of("Asia/Tokyo"))).thenReturn(sunday);
            mockedLocalTime.when(() -> LocalTime.now(ZoneId.of("Asia/Tokyo"))).thenReturn(dummyTime);

            // Act
            target.checkAndSendRemind();

            // Assert
            verifyNoInteractions(userSettingRepository);
            verifyNoInteractions(slackGateway);
            verifyNoInteractions(googleCalendarGateway);
            verifyNoInteractions(dailyLogRepository);
        }

        @Test
        @DisplayName("祝日または休暇と判定された場合は送信をスキップすること")
        void checkAndSendRemind_Holiday_ShouldSkip() {
            // Arrange (2026-06-09 is Tuesday)
            LocalDate weekday = LocalDate.of(2026, 6, 9);
            LocalTime time = LocalTime.of(18, 0);

            UserSetting user = UserSetting.builder().slackUserId("U123456").googleCalendarId("cal-123").active(true)
                    .remindTime("18:00").build();

            mockedLocalDate.when(() -> LocalDate.now(ZoneId.of("Asia/Tokyo"))).thenReturn(weekday);
            mockedLocalTime.when(() -> LocalTime.now(ZoneId.of("Asia/Tokyo"))).thenReturn(time);

            when(userSettingRepository.findAllActive()).thenReturn(Collections.singletonList(user));
            when(googleCalendarGateway.isHolidayOrPaidLeave("cal-123", weekday)).thenReturn(true);

            // Act
            target.checkAndSendRemind();

            // Assert
            verify(googleCalendarGateway).isHolidayOrPaidLeave("cal-123", weekday);
            verifyNoInteractions(slackGateway);
        }

        @Test
        @DisplayName("すでに日報が提出されている場合は送信をスキップすること")
        void checkAndSendRemind_AlreadySubmitted_ShouldSkip() {
            // Arrange (2026-06-09 is Tuesday)
            LocalDate weekday = LocalDate.of(2026, 6, 9);
            LocalTime time = LocalTime.of(18, 0);

            UserSetting user = UserSetting.builder().slackUserId("U123456").googleCalendarId("cal-123").active(true)
                    .remindTime("18:00").build();
            Log dummyLog = Log.builder().build();

            mockedLocalDate.when(() -> LocalDate.now(ZoneId.of("Asia/Tokyo"))).thenReturn(weekday);
            mockedLocalTime.when(() -> LocalTime.now(ZoneId.of("Asia/Tokyo"))).thenReturn(time);

            when(userSettingRepository.findAllActive()).thenReturn(Collections.singletonList(user));
            when(googleCalendarGateway.isHolidayOrPaidLeave("cal-123", weekday)).thenReturn(false);
            when(dailyLogRepository.findByUserIdAndDate("U123456", weekday)).thenReturn(Optional.of(dummyLog));

            // Act
            target.checkAndSendRemind();

            // Assert
            verify(dailyLogRepository).findByUserIdAndDate("U123456", weekday);
            verifyNoInteractions(slackGateway);
        }

        @Test
        @DisplayName("平日かつ送信条件を満たす場合にSlack通知されること")
        void checkAndSendRemind_Success_ShouldPostMessage() {
            // Arrange (2026-06-09 is Tuesday)
            LocalDate weekday = LocalDate.of(2026, 6, 9);
            LocalTime time = LocalTime.of(18, 0);

            UserSetting user = UserSetting.builder().slackUserId("U123456").googleCalendarId("cal-123").active(true)
                    .remindTime("18:00").build();

            mockedLocalDate.when(() -> LocalDate.now(ZoneId.of("Asia/Tokyo"))).thenReturn(weekday);
            mockedLocalTime.when(() -> LocalTime.now(ZoneId.of("Asia/Tokyo"))).thenReturn(time);

            when(userSettingRepository.findAllActive()).thenReturn(Collections.singletonList(user));
            when(googleCalendarGateway.isHolidayOrPaidLeave("cal-123", weekday)).thenReturn(false);
            when(dailyLogRepository.findByUserIdAndDate("U123456", weekday)).thenReturn(Optional.empty());

            // Act
            target.checkAndSendRemind();

            // Assert
            verify(slackGateway).postMessage("U123456", UserMessageConstants.REMIND_MESSAGE);
        }

        @Test
        @DisplayName("特定ユーザー処理中に例外が発生しても他のユーザーの処理が継続されること")
        void checkAndSendRemind_ExceptionInLoop_ShouldContinue() {
            // Arrange (2026-06-09 is Tuesday)
            LocalDate weekday = LocalDate.of(2026, 6, 9);
            LocalTime time = LocalTime.of(18, 0);

            UserSetting user1 = UserSetting.builder().slackUserId("U111111").googleCalendarId("cal-111").active(true)
                    .remindTime("18:00").build();

            UserSetting user2 = UserSetting.builder().slackUserId("U222222").googleCalendarId("cal-222").active(true)
                    .remindTime("18:00").build();

            mockedLocalDate.when(() -> LocalDate.now(ZoneId.of("Asia/Tokyo"))).thenReturn(weekday);
            mockedLocalTime.when(() -> LocalTime.now(ZoneId.of("Asia/Tokyo"))).thenReturn(time);

            when(userSettingRepository.findAllActive()).thenReturn(java.util.Arrays.asList(user1, user2));
            // User1 throws exception on calendar check
            when(googleCalendarGateway.isHolidayOrPaidLeave("cal-111", weekday))
                    .thenThrow(new RuntimeException("Calendar gateway error"));
            // User2 succeeds
            when(googleCalendarGateway.isHolidayOrPaidLeave("cal-222", weekday)).thenReturn(false);
            when(dailyLogRepository.findByUserIdAndDate("U222222", weekday)).thenReturn(Optional.empty());

            // Act
            target.checkAndSendRemind();

            // Assert
            verify(slackGateway, never()).postMessage(eq("U111111"), anyString());
            verify(slackGateway).postMessage("U222222", UserMessageConstants.REMIND_MESSAGE);
        }
    }
}
