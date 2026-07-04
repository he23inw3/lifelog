package jp.he23inw3.asset.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import jp.he23inw3.asset.domain.gateway.GeminiGateway;
import jp.he23inw3.asset.domain.gateway.SlackGateway;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.DailyLogRepository;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import jp.he23inw3.asset.domain.repository.dto.DailyLogSearchQuery;
import jp.he23inw3.asset.domain.service.DailyLogDomainService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonthlyReflectionUseCaseTest {

    @Mock
    UserSettingRepository userSettingRepository;

    @Mock
    DailyLogRepository dailyLogRepository;

    @Mock
    DailyLogDomainService dailyLogDomainService;

    @Mock
    GeminiGateway geminiGateway;

    @Mock
    SlackGateway slackGateway;

    @InjectMocks
    MonthlyReflectionUseCase target;

    private MockedStatic<LocalDate> mockedLocalDate;

    @BeforeEach
    void setUp() {
        mockedLocalDate = mockStatic(LocalDate.class, CALLS_REAL_METHODS);
    }

    @AfterEach
    void tearDown() {
        mockedLocalDate.close();
    }

    @Nested
    @DisplayName("月次振り返りレポート自動作成")
    class CreateMonthlyReport {

        @Test
        @DisplayName("月末日でない場合は処理をスキップすること")
        void createMonthlyReport_NotLastDay_ShouldSkip() {
            // Arrange (2026-06-09 is not last day of 30-day month June)
            LocalDate notLastDay = LocalDate.of(2026, 6, 9);

            mockedLocalDate.when(() -> LocalDate.now(ZoneId.of("Asia/Tokyo"))).thenReturn(notLastDay);
            when(dailyLogDomainService.isLastDayOfMonth(notLastDay)).thenReturn(false);

            // Act
            target.createMonthlyReport();

            // Assert
            verify(dailyLogDomainService).isLastDayOfMonth(notLastDay);
            verifyNoInteractions(userSettingRepository);
            verifyNoInteractions(dailyLogRepository);
            verifyNoInteractions(geminiGateway);
            verifyNoInteractions(slackGateway);
        }

        @Test
        @DisplayName("月末日の場合、正常にレポート作成および送信が行われること")
        void createMonthlyReport_LastDay_Success() {
            // Arrange (2026-06-30 is last day of June)
            LocalDate lastDay = LocalDate.of(2026, 6, 30);

            UserSetting user = UserSetting.builder().slackUserId("U123456").active(true).build();

            Log dailyLog = Log.builder().logDate(LocalDate.of(2026, 6, 15)).tasks("Completed task A").diary("Good day")
                    .build();

            mockedLocalDate.when(() -> LocalDate.now(ZoneId.of("Asia/Tokyo"))).thenReturn(lastDay);

            when(dailyLogDomainService.isLastDayOfMonth(lastDay)).thenReturn(true);
            when(userSettingRepository.findAllActive()).thenReturn(Collections.singletonList(user));

            ArgumentCaptor<DailyLogSearchQuery> queryCaptor = ArgumentCaptor.forClass(DailyLogSearchQuery.class);
            when(dailyLogRepository.findByUserIdAndPeriod(queryCaptor.capture()))
                    .thenReturn(Collections.singletonList(dailyLog));

            when(dailyLogDomainService.formatReflectionLogs(Collections.singletonList(dailyLog)))
                    .thenReturn("Formatted Completed task A");
            when(geminiGateway.generateMonthlyReport("Formatted Completed task A")).thenReturn("This is Gemini's report summary");

            // Act
            target.createMonthlyReport();

            // Assert
            // Query verification
            DailyLogSearchQuery capturedQuery = queryCaptor.getValue();
            assertThat(capturedQuery.getSlackUserId()).isEqualTo("U123456");
            assertThat(capturedQuery.getStart()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(capturedQuery.getEnd()).isEqualTo(LocalDate.of(2026, 6, 30));

            // Gemini and Slack verification
            verify(dailyLogDomainService).formatReflectionLogs(Collections.singletonList(dailyLog));
            verify(geminiGateway).generateMonthlyReport("Formatted Completed task A");
            verify(slackGateway).postMessage("U123456", "This is Gemini's report summary");
        }

        @Test
        @DisplayName("当月のログが存在しないユーザーについては処理をスキップすること")
        void createMonthlyReport_NoLogs_ShouldSkip() {
            // Arrange
            LocalDate lastDay = LocalDate.of(2026, 6, 30);
            UserSetting user = UserSetting.builder().slackUserId("U123456").active(true).build();

            mockedLocalDate.when(() -> LocalDate.now(ZoneId.of("Asia/Tokyo"))).thenReturn(lastDay);

            when(dailyLogDomainService.isLastDayOfMonth(lastDay)).thenReturn(true);
            when(userSettingRepository.findAllActive()).thenReturn(Collections.singletonList(user));
            when(dailyLogRepository.findByUserIdAndPeriod(any(DailyLogSearchQuery.class)))
                    .thenReturn(Collections.emptyList());

            // Act
            target.createMonthlyReport();

            // Assert
            verifyNoInteractions(geminiGateway);
            verifyNoInteractions(slackGateway);
        }

        @Test
        @DisplayName("特定ユーザー処理中に例外が発生しても他のユーザーの処理が継続されること")
        void createMonthlyReport_ExceptionInLoop_ShouldContinue() {
            // Arrange
            LocalDate lastDay = LocalDate.of(2026, 6, 30);
            UserSetting user1 = UserSetting.builder().slackUserId("U111111").active(true).build();
            UserSetting user2 = UserSetting.builder().slackUserId("U222222").active(true).build();

            Log log2 = Log.builder().logDate(LocalDate.of(2026, 6, 15)).tasks("Task B").build();

            mockedLocalDate.when(() -> LocalDate.now(ZoneId.of("Asia/Tokyo"))).thenReturn(lastDay);

            when(dailyLogDomainService.isLastDayOfMonth(lastDay)).thenReturn(true);
            when(userSettingRepository.findAllActive()).thenReturn(java.util.Arrays.asList(user1, user2));

            // User1 throws exception
            when(dailyLogRepository.findByUserIdAndPeriod(argThat(q -> q != null && "U111111".equals(q.getSlackUserId()))))
                    .thenThrow(new RuntimeException("Database error"));

            // User2 succeeds
            when(dailyLogRepository.findByUserIdAndPeriod(argThat(q -> q != null && "U222222".equals(q.getSlackUserId()))))
                    .thenReturn(Collections.singletonList(log2));
            when(dailyLogDomainService.formatReflectionLogs(Collections.singletonList(log2)))
                    .thenReturn("Formatted Task B");
            when(geminiGateway.generateMonthlyReport("Formatted Task B")).thenReturn("Report 2");

            // Act
            target.createMonthlyReport();

            // Assert
            verify(slackGateway, never()).postMessage(eq("U111111"), anyString());
            verify(slackGateway).postMessage("U222222", "Report 2");
        }
    }
}
