package jp.he23inw3.asset.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.gateway.GeminiGateway;
import jp.he23inw3.asset.domain.gateway.GoogleCalendarGateway;
import jp.he23inw3.asset.domain.gateway.SlackGateway;
import jp.he23inw3.asset.domain.model.*;
import jp.he23inw3.asset.domain.repository.UserSessionRepository;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
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
class LifelogWorkflowUseCaseTest {

    @Mock
    UserSettingRepository userSettingRepository;

    @Mock
    UserSessionRepository userSessionRepository;

    @Mock
    GoogleCalendarGateway googleCalendarGateway;

    @Mock
    GeminiGateway geminiGateway;

    @Mock
    SlackGateway slackGateway;

    @Mock
    DailyLogDomainService dailyLogDomainService;

    @InjectMocks
    LifelogWorkflowUseCase target;

    private MockedStatic<LocalDateTime> mockedLocalDateTime;

    @BeforeEach
    void setUp() {
        mockedLocalDateTime = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS);
    }

    @AfterEach
    void tearDown() {
        mockedLocalDateTime.close();
    }

    @Nested
    @DisplayName("対話型ライフログ登録処理")
    class ProcessLifelog {

        @Test
        @DisplayName("ユーザー設定が存在しない場合、Slackにエラー通知して例外をスローすること")
        void processLifelog_UserSettingNotFound_ShouldThrowException() {
            // Arrange
            String slackUserId = "U999999";
            when(userSettingRepository.findById(slackUserId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> target.processLifelog(slackUserId, "some text"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User settings not found for " + slackUserId);

            verify(slackGateway).postMessage(eq(slackUserId), contains("ユーザー設定が見つかりません"));
        }

        @Test
        @DisplayName("日報に関係のない入力の場合、セッションをクリアして終了すること")
        void processLifelog_NotLogRelated_ShouldClearSession() {
            // Arrange
            String slackUserId = "U123456";
            UserSetting setting = UserSetting.builder().slackUserId(slackUserId).googleCalendarId("cal-123").build();
            when(userSettingRepository.findById(slackUserId)).thenReturn(Optional.of(setting));
            when(userSessionRepository.findById(slackUserId)).thenReturn(Optional.empty());

            LocalDateTime now = LocalDateTime.of(2026, 6, 9, 12, 0);
            mockedLocalDateTime.when(() -> LocalDateTime.now(ZoneId.of("Asia/Tokyo"))).thenReturn(now);
            when(dailyLogDomainService.determineHolidayStatus(eq(now.toLocalDate()), eq(false), eq("cal-123")))
                    .thenReturn(false);

            GeminiParseResult parseResult = GeminiParseResult.builder().logRelated(false).build();
            when(geminiGateway.parse(anyString(), eq(now), eq(DayStatus.WEEKDAY.getValue()))).thenReturn(parseResult);

            // Act
            target.processLifelog(slackUserId, "挨拶だけするね");

            // Assert
            verify(userSessionRepository).delete(slackUserId);
            verifyNoInteractions(slackGateway);
        }

        @Test
        @DisplayName("入力情報が不足している場合、セッションをWAITING_WORK_HOURSにして問いかけること")
        void processLifelog_InsufficientInput_ShouldSaveSessionAndAskHours() {
            // Arrange
            String slackUserId = "U123456";
            UserSetting setting = UserSetting.builder().slackUserId(slackUserId).googleCalendarId("cal-123").build();
            when(userSettingRepository.findById(slackUserId)).thenReturn(Optional.of(setting));
            when(userSessionRepository.findById(slackUserId)).thenReturn(Optional.empty());

            LocalDateTime now = LocalDateTime.of(2026, 6, 9, 12, 0);
            mockedLocalDateTime.when(() -> LocalDateTime.now(ZoneId.of("Asia/Tokyo"))).thenReturn(now);
            when(dailyLogDomainService.determineHolidayStatus(eq(now.toLocalDate()), eq(false), eq("cal-123")))
                    .thenReturn(false);
            when(dailyLogDomainService.isInputInsufficient(any(GeminiParseResult.class), eq(false))).thenReturn(true);

            GeminiParseResult parseResult = GeminiParseResult.builder().logRelated(true).holiday(false).workHours(0.0)
                    .replyMessage("稼働時間は何時間ですか？").build();
            when(geminiGateway.parse(anyString(), eq(now), eq(DayStatus.WEEKDAY.getValue()))).thenReturn(parseResult);

            // Act
            target.processLifelog(slackUserId, "日報です。タスクAをやりました。");

            // Assert
            ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
            verify(userSessionRepository).save(sessionCaptor.capture());

            Session savedSession = sessionCaptor.getValue();
            assertThat(savedSession.getSlackUserId()).isEqualTo(slackUserId);
            assertThat(savedSession.getStatus()).isEqualTo(SessionStatus.WAITING_WORK_HOURS);
            assertThat(savedSession.getTempData().get("rawText")).isEqualTo("日報です。タスクAをやりました。");

            verify(slackGateway).postMessage(slackUserId, "稼働時間は何時間ですか？");
        }

        @Test
        @DisplayName("入力が十分な場合、セッションをAWAITING_CONFIRMATIONにして確認メッセージを送信すること")
        void processLifelog_SufficientInput_ShouldRequestConfirmation() {
            // Arrange
            String slackUserId = "U123456";
            UserSetting setting = UserSetting.builder().slackUserId(slackUserId).googleCalendarId("cal-123").build();
            when(userSettingRepository.findById(slackUserId)).thenReturn(Optional.of(setting));
            when(userSessionRepository.findById(slackUserId)).thenReturn(Optional.empty());

            LocalDateTime now = LocalDateTime.of(2026, 6, 9, 12, 0);
            mockedLocalDateTime.when(() -> LocalDateTime.now(ZoneId.of("Asia/Tokyo"))).thenReturn(now);
            when(dailyLogDomainService.determineHolidayStatus(eq(now.toLocalDate()), eq(false), eq("cal-123")))
                    .thenReturn(false);
            when(dailyLogDomainService.isInputInsufficient(any(GeminiParseResult.class), eq(false))).thenReturn(false);

            GeminiParseResult parseResult = GeminiParseResult.builder().logRelated(true).holiday(false)
                    .logDate("2026-06-09").tasks("タスクA").workHours(8.0).overtimeHours(1.0).diary("楽しかった")
                    .sentiment(Sentiment.HAPPY).replyMessage("確認してください。").build();
            when(geminiGateway.parse(anyString(), eq(now), eq(DayStatus.WEEKDAY.getValue()))).thenReturn(parseResult);

            // Act
            target.processLifelog(slackUserId, "タスクA 8時間 楽しかった");

            // Assert
            ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
            verify(userSessionRepository).save(sessionCaptor.capture());

            Session savedSession = sessionCaptor.getValue();
            assertThat(savedSession.getStatus()).isEqualTo(SessionStatus.AWAITING_CONFIRMATION);
            assertThat(savedSession.getTempData().get("workHours")).isEqualTo("8.0");
            assertThat(savedSession.getTempData().get("isHoliday")).isEqualTo("false");

            verify(slackGateway).postConfirmationMessage(slackUserId, "2026-06-09", "タスクA", 8.0, "楽しかった", false);
        }

        @Test
        @DisplayName("1時間以内の既存セッションがある場合、テキストがマージされること")
        void processLifelog_MergeSession_Success() {
            // Arrange
            String slackUserId = "U123456";
            UserSetting setting = UserSetting.builder().slackUserId(slackUserId).googleCalendarId("cal-123").build();
            when(userSettingRepository.findById(slackUserId)).thenReturn(Optional.of(setting));

            Map<String, String> existingData = new HashMap<>();
            existingData.put("rawText", "前の発言");
            Session existingSession = Session.builder().slackUserId(slackUserId).status(SessionStatus.WAITING_WORK_HOURS)
                    .updatedAt(Instant.now().minusSeconds(1800))
                    .tempData(existingData).build();
            when(userSessionRepository.findById(slackUserId)).thenReturn(Optional.of(existingSession));

            LocalDateTime now = LocalDateTime.of(2026, 6, 9, 12, 0);
            mockedLocalDateTime.when(() -> LocalDateTime.now(ZoneId.of("Asia/Tokyo"))).thenReturn(now);
            when(dailyLogDomainService.determineHolidayStatus(eq(now.toLocalDate()), eq(false), eq("cal-123")))
                    .thenReturn(false);
            when(dailyLogDomainService.isInputInsufficient(any(GeminiParseResult.class), eq(false))).thenReturn(false);

            GeminiParseResult parseResult = GeminiParseResult.builder().logRelated(true).holiday(false)
                    .logDate("2026-06-09").tasks("タスクB").workHours(8.0).build();
            when(geminiGateway.parse(contains("前の発言\n新しい発言"), eq(now), anyString())).thenReturn(parseResult);

            // Act
            target.processLifelog(slackUserId, "新しい発言");

            // Assert
            verify(geminiGateway).parse(eq("前の発言\n新しい発言"), eq(now), anyString());
        }

        @Test
        @DisplayName("セッションが期限切れの場合、メッセージ受信時に削除・通知して新規登録を開始すること")
        void processLifelog_SessionExpired_ShouldDeleteSessionAndNotifyUserAndStartNewSession() {
            // Arrange
            String slackUserId = "U123456";
            UserSetting setting = UserSetting.builder().slackUserId(slackUserId).googleCalendarId("cal-123").build();
            when(userSettingRepository.findById(slackUserId)).thenReturn(Optional.of(setting));

            Session expiredSession = Session.builder().slackUserId(slackUserId).status(SessionStatus.WAITING_WORK_HOURS)
                    .expiresAt(Instant.now().minusSeconds(1))
                    .tempData(new HashMap<>()).build();
            when(userSessionRepository.findById(slackUserId)).thenReturn(Optional.of(expiredSession));

            LocalDateTime now = LocalDateTime.of(2026, 6, 9, 12, 0);
            mockedLocalDateTime.when(() -> LocalDateTime.now(ZoneId.of("Asia/Tokyo"))).thenReturn(now);
            when(dailyLogDomainService.determineHolidayStatus(eq(now.toLocalDate()), eq(false), eq("cal-123")))
                    .thenReturn(false);
            when(dailyLogDomainService.isInputInsufficient(any(GeminiParseResult.class), eq(false))).thenReturn(false);

            GeminiParseResult parseResult = GeminiParseResult.builder().logRelated(true).holiday(false).workHours(8.0)
                    .logDate("2026-06-09").tasks("タスクA").diary("楽しかった").build();
            when(geminiGateway.parse(eq("新しい発言"), eq(now), anyString())).thenReturn(parseResult);

            // Act
            target.processLifelog(slackUserId, "新しい発言");

            // Assert
            verify(userSessionRepository).delete(slackUserId);
            verify(slackGateway).postMessage(slackUserId,
                    "前回の入力から時間が空いていたため、前回の対話は終了しています。\n" + "このメッセージは新しい記録として受け付けます。\n\n" + "日報や日記として記録したい内容を教えてください。");
            ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
            verify(userSessionRepository).save(sessionCaptor.capture());
            Session savedSession = sessionCaptor.getValue();
            assertThat(savedSession.getStatus()).isEqualTo(SessionStatus.AWAITING_CONFIRMATION);
            assertThat(savedSession.getExpiresAt()).isAfter(Instant.now().plusSeconds(3500));
        }

        @Test
        @DisplayName("作業日が不足している場合、作業日の入力を促すこと")
        void processLifelog_MissingDate_ShouldPromptDate() {
            // Arrange
            String slackUserId = "U123456";
            UserSetting setting = UserSetting.builder().slackUserId(slackUserId).googleCalendarId("cal-123").build();
            when(userSettingRepository.findById(slackUserId)).thenReturn(Optional.of(setting));
            when(userSessionRepository.findById(slackUserId)).thenReturn(Optional.empty());

            LocalDateTime now = LocalDateTime.of(2026, 6, 9, 12, 0);
            mockedLocalDateTime.when(() -> LocalDateTime.now(ZoneId.of("Asia/Tokyo"))).thenReturn(now);
            when(dailyLogDomainService.determineHolidayStatus(eq(now.toLocalDate()), eq(false), eq("cal-123")))
                    .thenReturn(false);
            when(dailyLogDomainService.isInputInsufficient(any(GeminiParseResult.class), eq(false))).thenReturn(true);
            when(dailyLogDomainService.buildMissingFieldsMessage(any(GeminiParseResult.class), eq(false)))
                    .thenReturn("作業日を入力してください。");

            GeminiParseResult parseResult = GeminiParseResult.builder().logRelated(true).holiday(false).logDate(null)
                    .tasks("タスクA").workHours(8.0).replyMessage(null).build();
            when(geminiGateway.parse(anyString(), eq(now), eq(DayStatus.WEEKDAY.getValue()))).thenReturn(parseResult);

            // Act
            target.processLifelog(slackUserId, "タスクAを8時間やりました。");

            // Assert
            verify(slackGateway).postMessage(slackUserId, "作業日を入力してください。");
        }

        @Test
        @DisplayName("作業内容が不足している場合、作業内容の入力を促すこと")
        void processLifelog_MissingTasks_ShouldPromptTasks() {
            // Arrange
            String slackUserId = "U123456";
            UserSetting setting = UserSetting.builder().slackUserId(slackUserId).googleCalendarId("cal-123").build();
            when(userSettingRepository.findById(slackUserId)).thenReturn(Optional.of(setting));
            when(userSessionRepository.findById(slackUserId)).thenReturn(Optional.empty());

            LocalDateTime now = LocalDateTime.of(2026, 6, 9, 12, 0);
            mockedLocalDateTime.when(() -> LocalDateTime.now(ZoneId.of("Asia/Tokyo"))).thenReturn(now);
            when(dailyLogDomainService.determineHolidayStatus(eq(now.toLocalDate()), eq(false), eq("cal-123")))
                    .thenReturn(false);
            when(dailyLogDomainService.isInputInsufficient(any(GeminiParseResult.class), eq(false))).thenReturn(true);
            when(dailyLogDomainService.buildMissingFieldsMessage(any(GeminiParseResult.class), eq(false)))
                    .thenReturn("作業内容を入力してください。");

            GeminiParseResult parseResult = GeminiParseResult.builder().logRelated(true).holiday(false)
                    .logDate("2026-06-09").tasks(null).workHours(8.0).replyMessage(null).build();
            when(geminiGateway.parse(anyString(), eq(now), eq(DayStatus.WEEKDAY.getValue()))).thenReturn(parseResult);

            // Act
            target.processLifelog(slackUserId, "今日、8時間働きました。");

            // Assert
            verify(slackGateway).postMessage(slackUserId, "作業内容を入力してください。");
        }

        @Test
        @DisplayName("作業日と作業内容が不足している場合、両方の入力を促すこと")
        void processLifelog_MissingMultiple_ShouldPromptMultiple() {
            // Arrange
            String slackUserId = "U123456";
            UserSetting setting = UserSetting.builder().slackUserId(slackUserId).googleCalendarId("cal-123").build();
            when(userSettingRepository.findById(slackUserId)).thenReturn(Optional.of(setting));
            when(userSessionRepository.findById(slackUserId)).thenReturn(Optional.empty());

            LocalDateTime now = LocalDateTime.of(2026, 6, 9, 12, 0);
            mockedLocalDateTime.when(() -> LocalDateTime.now(ZoneId.of("Asia/Tokyo"))).thenReturn(now);
            when(dailyLogDomainService.determineHolidayStatus(eq(now.toLocalDate()), eq(false), eq("cal-123")))
                    .thenReturn(false);
            when(dailyLogDomainService.isInputInsufficient(any(GeminiParseResult.class), eq(false))).thenReturn(true);
            when(dailyLogDomainService.buildMissingFieldsMessage(any(GeminiParseResult.class), eq(false)))
                    .thenReturn("作業日と作業内容を入力してください。");

            GeminiParseResult parseResult = GeminiParseResult.builder().logRelated(true).holiday(false).logDate(null)
                    .tasks(null).workHours(8.0).replyMessage(null).build();
            when(geminiGateway.parse(anyString(), eq(now), eq(DayStatus.WEEKDAY.getValue()))).thenReturn(parseResult);

            // Act
            target.processLifelog(slackUserId, "8時間働きました。");

            // Assert
            verify(slackGateway).postMessage(slackUserId, "作業日と作業内容を入力してください。");
        }

        @Test
        @DisplayName("Gemini解析中に例外が発生した場合、Slackにエラー通知して処理を終了すること")
        void processLifelog_GeminiException_ShouldNotifyError() {
            // Arrange
            String slackUserId = "U123456";
            UserSetting setting = UserSetting.builder().slackUserId(slackUserId).googleCalendarId("cal-123").build();
            when(userSettingRepository.findById(slackUserId)).thenReturn(Optional.of(setting));
            when(userSessionRepository.findById(slackUserId)).thenReturn(Optional.empty());

            LocalDateTime now = LocalDateTime.of(2026, 6, 9, 12, 0);
            mockedLocalDateTime.when(() -> LocalDateTime.now(ZoneId.of("Asia/Tokyo"))).thenReturn(now);
            when(dailyLogDomainService.determineHolidayStatus(eq(now.toLocalDate()), eq(false), eq("cal-123")))
                    .thenReturn(false);

            when(geminiGateway.parse(anyString(), eq(now), eq(DayStatus.WEEKDAY.getValue())))
                    .thenThrow(new RuntimeException("Gemini API Error"));

            // Act
            target.processLifelog(slackUserId, "日報です。");

            // Assert
            verify(slackGateway).postMessage(eq(slackUserId), contains("日報の自動解析中にエラーが発生しました"));
            verifyNoMoreInteractions(userSessionRepository);
        }
    }

    @Nested
    @DisplayName("ライフログ確定処理")
    class ConfirmRegistration {

        @Test
        @DisplayName("確定処理 - セッションが見つからない場合はエラーを通知すること")
        void confirmRegistration_SessionNotFound_ShouldNotifyError() {
            // Arrange
            String slackUserId = "U123456";
            String responseUrl = "http://slack.com/actions";
            when(userSessionRepository.findById(slackUserId)).thenReturn(Optional.empty());

            // Act
            target.confirmRegistration(slackUserId, responseUrl);

            // Assert
            verify(slackGateway).updateMessage(responseUrl, "セッションが見つからないか、期限切れです。もう一度やり直してください。");
        }

        @Test
        @DisplayName("確定処理 - 正常系")
        void confirmRegistration_Success() {
            // Arrange
            String slackUserId = "U123456";
            String responseUrl = "http://slack.com/actions";

            UserSetting setting = UserSetting.builder().slackUserId(slackUserId).googleCalendarId("cal-123").build();
            when(userSettingRepository.findById(slackUserId)).thenReturn(Optional.of(setting));

            Map<String, String> tempData = new HashMap<>();
            tempData.put("logDate", "2026-06-09");
            tempData.put("isHoliday", "false");
            tempData.put("tasks", "タスクA");
            tempData.put("workHours", "8.0");
            tempData.put("overtimeHours", "1.0");
            tempData.put("diary", "日記内容");
            tempData.put("sentiment", "Happy");
            tempData.put("rawText", "タスクA 8時間");
            tempData.put("replyMessage", "お疲れ様でした！");

            Session session = Session.builder().slackUserId(slackUserId).status(SessionStatus.AWAITING_CONFIRMATION)
                    .tempData(tempData).build();
            when(userSessionRepository.findById(slackUserId)).thenReturn(Optional.of(session));

            // Act
            target.confirmRegistration(slackUserId, responseUrl);

            // Assert
            verify(dailyLogDomainService).registerCalendarEvent(eq("cal-123"), eq(LocalDate.of(2026, 6, 9)), any(GeminiParseResult.class));
            verify(dailyLogDomainService).saveDailyLog(eq(slackUserId), eq(LocalDate.of(2026, 6, 9)), eq("タスクA 8時間"), any(GeminiParseResult.class), eq("cal-123"));

            verify(userSessionRepository).delete(slackUserId);
            verify(slackGateway).updateMessage(responseUrl, "確定しました。");
            verify(slackGateway).postMessage(slackUserId, "お疲れ様でした！");
        }

        @Test
        @DisplayName("確定処理 - セッションが期限切れの場合は削除してエラー通知すること")
        void confirmRegistration_SessionExpired_ShouldDeleteAndNotifyError() {
            // Arrange
            String slackUserId = "U123456";
            String responseUrl = "http://slack.com/actions";
            Session expiredSession = Session.builder().slackUserId(slackUserId).status(SessionStatus.AWAITING_CONFIRMATION)
                    .expiresAt(Instant.now().minusSeconds(1))
                    .build();
            when(userSessionRepository.findById(slackUserId)).thenReturn(Optional.of(expiredSession));

            // Act
            target.confirmRegistration(slackUserId, responseUrl);

            // Assert
            verify(userSessionRepository).delete(slackUserId);
            verify(slackGateway).updateMessage(responseUrl, "セッションが見つからないか、期限切れです。もう一度やり直してください。");
        }
    }

    @Nested
    @DisplayName("ライフログキャンセル処理")
    class CancelRegistration {

        @Test
        @DisplayName("キャンセル処理 - セッションを削除し通知を更新すること")
        void cancelRegistration_Success() {
            // Arrange
            String slackUserId = "U123456";
            String responseUrl = "http://slack.com/actions";

            // Act
            target.cancelRegistration(slackUserId, responseUrl);

            // Assert
            verify(userSessionRepository).delete(slackUserId);
            verify(slackGateway).updateMessage(responseUrl, "日報の登録をキャンセルしました。");
        }
    }
}
