package jp.he23inw3.asset.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import jp.he23inw3.asset.domain.exception.DailyLogValidationException;
import jp.he23inw3.asset.domain.gateway.GoogleCalendarGateway;
import jp.he23inw3.asset.domain.model.GeminiParseResult;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.model.Sentiment;
import jp.he23inw3.asset.domain.repository.DailyLogRepository;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DailyLogDomainServiceTest {

    @Mock
    GoogleCalendarGateway googleCalendarGateway;

    @Mock
    DailyLogRepository dailyLogRepository;

    @InjectMocks
    DailyLogDomainService target;

    @Nested
    @DisplayName("日報登録・変更可能期間の検証")
    class ValidateModificationPeriod {

        @Test
        @DisplayName("当月の日付の場合、例外が発生しないこと")
        void validateModificationPeriod_CurrentMonth() {
            LocalDate currentMonthDate = LocalDate.of(2026, 6, 12);
            try (MockedStatic<DateTimeUtil> mocked = mockStatic(DateTimeUtil.class)) {
                mocked.when(DateTimeUtil::nowLocalDate).thenReturn(LocalDate.of(2026, 6, 25));

                assertThatCode(() -> target.validateModificationPeriod(currentMonthDate))
                        .doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("過去月の日付の場合、通常はDailyLogValidationExceptionが発生すること")
        void validateModificationPeriod_PastMonth_NormalDay() {
            LocalDate pastMonthDate = LocalDate.of(2026, 5, 12);
            try (MockedStatic<DateTimeUtil> mocked = mockStatic(DateTimeUtil.class)) {
                mocked.when(DateTimeUtil::nowLocalDate).thenReturn(LocalDate.of(2026, 6, 25)); // 25日（2日以降）

                assertThatThrownBy(() -> target.validateModificationPeriod(pastMonthDate))
                        .isInstanceOf(DailyLogValidationException.class)
                        .hasMessageContaining("過去月の日報は変更できません。当月内の日報のみ変更可能です。");
            }
        }

        @Test
        @DisplayName("本日が当月1日かつ対象が前月の日付の場合、例外が発生しないこと")
        void validateModificationPeriod_PreviousMonth_OnFirstDay() {
            LocalDate previousMonthDate = LocalDate.of(2026, 5, 31);
            try (MockedStatic<DateTimeUtil> mocked = mockStatic(DateTimeUtil.class)) {
                mocked.when(DateTimeUtil::nowLocalDate).thenReturn(LocalDate.of(2026, 6, 1)); // 当月1日

                assertThatCode(() -> target.validateModificationPeriod(previousMonthDate))
                        .doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("本日が当月1日であっても、対象が前々月以前の日付の場合、例外が発生すること")
        void validateModificationPeriod_TwoMonthsAgo_OnFirstDay() {
            LocalDate twoMonthsAgoDate = LocalDate.of(2026, 4, 30);
            try (MockedStatic<DateTimeUtil> mocked = mockStatic(DateTimeUtil.class)) {
                mocked.when(DateTimeUtil::nowLocalDate).thenReturn(LocalDate.of(2026, 6, 1)); // 当月1日

                assertThatThrownBy(() -> target.validateModificationPeriod(twoMonthsAgoDate))
                        .isInstanceOf(DailyLogValidationException.class)
                        .hasMessageContaining("過去月の日報は変更できません。当月内の日報のみ変更可能です。");
            }
        }

        @Test
        @DisplayName("本日が当月2日以降の場合、対象が前月の日付であっても、例外が発生すること")
        void validateModificationPeriod_PreviousMonth_OnSecondDay() {
            LocalDate previousMonthDate = LocalDate.of(2026, 5, 31);
            try (MockedStatic<DateTimeUtil> mocked = mockStatic(DateTimeUtil.class)) {
                mocked.when(DateTimeUtil::nowLocalDate).thenReturn(LocalDate.of(2026, 6, 2)); // 当月2日

                assertThatThrownBy(() -> target.validateModificationPeriod(previousMonthDate))
                        .isInstanceOf(DailyLogValidationException.class)
                        .hasMessageContaining("過去月の日報は変更できません。当月内の日報のみ変更可能です。");
            }
        }

        @Test
        @DisplayName("翌月の日付の場合、DailyLogValidationExceptionが発生すること")
        void validateModificationPeriod_FutureMonth() {
            LocalDate futureMonthDate = LocalDate.of(2026, 7, 1);
            try (MockedStatic<DateTimeUtil> mocked = mockStatic(DateTimeUtil.class)) {
                mocked.when(DateTimeUtil::nowLocalDate).thenReturn(LocalDate.of(2026, 6, 25));

                assertThatThrownBy(() -> target.validateModificationPeriod(futureMonthDate))
                        .isInstanceOf(DailyLogValidationException.class)
                        .hasMessageContaining("未来日の日報は登録できません。");
            }
        }

        @Test
        @DisplayName("当月の未来日の日付の場合、DailyLogValidationExceptionが発生すること")
        void validateModificationPeriod_FutureDayInCurrentMonth() {
            LocalDate futureDayInCurrentMonth = LocalDate.of(2026, 6, 26);
            try (MockedStatic<DateTimeUtil> mocked = mockStatic(DateTimeUtil.class)) {
                mocked.when(DateTimeUtil::nowLocalDate).thenReturn(LocalDate.of(2026, 6, 25));

                assertThatThrownBy(() -> target.validateModificationPeriod(futureDayInCurrentMonth))
                        .isInstanceOf(DailyLogValidationException.class)
                        .hasMessageContaining("未来日の日報は登録できません。");
            }
        }
    }

    @Nested
    @DisplayName("休日・休暇状態の判定")
    class DetermineHolidayStatus {

        @Test
        @DisplayName("土曜日の場合はtrueを返すこと")
        void Saturday_ReturnsTrue() {
            LocalDate date = LocalDate.of(2026, 6, 27); // 土曜日
            assertThat(target.determineHolidayStatus(date, false, "cal-id")).isTrue();
        }

        @Test
        @DisplayName("日曜日の場合はtrueを返すこと")
        void Sunday_ReturnsTrue() {
            LocalDate date = LocalDate.of(2026, 6, 28); // 日曜日
            assertThat(target.determineHolidayStatus(date, false, "cal-id")).isTrue();
        }

        @Test
        @DisplayName("平日でisHolidayCommandがtrueの場合はtrueを返すこと")
        void Weekday_IsHolidayCommandTrue_ReturnsTrue() {
            LocalDate date = LocalDate.of(2026, 6, 29); // 月曜日
            assertThat(target.determineHolidayStatus(date, true, "cal-id")).isTrue();
        }

        @Test
        @DisplayName("平日でisHolidayCommandがfalseで、カレンダー休暇判定がtrueの場合はtrueを返すこと")
        void Weekday_CalendarHolidayTrue_ReturnsTrue() {
            LocalDate date = LocalDate.of(2026, 6, 29); // 月曜日
            when(googleCalendarGateway.isHolidayOrPaidLeave("cal-id", date)).thenReturn(true);
            assertThat(target.determineHolidayStatus(date, false, "cal-id")).isTrue();
        }

        @Test
        @DisplayName("平日でisHolidayCommandがfalseで、カレンダー休暇判定がfalseの場合はfalseを返すこと")
        void Weekday_CalendarHolidayFalse_ReturnsFalse() {
            LocalDate date = LocalDate.of(2026, 6, 29); // 月曜日
            when(googleCalendarGateway.isHolidayOrPaidLeave("cal-id", date)).thenReturn(false);
            assertThat(target.determineHolidayStatus(date, false, "cal-id")).isFalse();
        }

        @Test
        @DisplayName("平日でカレンダー休暇判定が例外をスローした場合、キャッチしてfalse(平日扱い)とすること")
        void Weekday_CalendarThrowsException_ReturnsFalse() {
            LocalDate date = LocalDate.of(2026, 6, 29); // 月曜日
            when(googleCalendarGateway.isHolidayOrPaidLeave("cal-id", date))
                    .thenThrow(new RuntimeException("API error"));
            assertThat(target.determineHolidayStatus(date, false, "cal-id")).isFalse();
        }
    }

    @Nested
    @DisplayName("入力情報不足の判定")
    class IsInputInsufficient {

        @Test
        @DisplayName("日付が空の場合はtrue(不足)を返すこと")
        void LogDate_Blank_ReturnsTrue() {
            GeminiParseResult result = GeminiParseResult.builder().logDate(null).build();
            assertThat(target.isInputInsufficient(result, false)).isTrue();
        }

        @Test
        @DisplayName("平日の場合、稼働時間または作業内容がなければtrue(不足)を返すこと")
        void Weekday_MissingHoursOrTasks_ReturnsTrue() {
            GeminiParseResult res1 = GeminiParseResult.builder().logDate("2026-06-29").workHours(0).tasks("プログラミング")
                    .build();
            GeminiParseResult res2 = GeminiParseResult.builder().logDate("2026-06-29").workHours(7.5).tasks("").build();

            assertThat(target.isInputInsufficient(res1, false)).isTrue();
            assertThat(target.isInputInsufficient(res2, false)).isTrue();
        }

        @Test
        @DisplayName("平日の場合、稼働時間と作業内容の両方があればfalse(十分)を返すこと")
        void Weekday_HasBoth_ReturnsFalse() {
            GeminiParseResult result = GeminiParseResult.builder().logDate("2026-06-29").workHours(7.5).tasks("プログラミング")
                    .build();
            assertThat(target.isInputInsufficient(result, false)).isFalse();
        }

        @Test
        @DisplayName("休日の場合、作業内容のみ、または稼働時間のみがある場合はtrue(不足)を返すこと")
        void Holiday_OnlyOnePresent_ReturnsTrue() {
            GeminiParseResult res1 = GeminiParseResult.builder().logDate("2026-06-30").holiday(true).workHours(0)
                    .tasks("勉強").build();
            GeminiParseResult res2 = GeminiParseResult.builder().logDate("2026-06-30").holiday(true).workHours(2.0)
                    .tasks("").build();

            assertThat(target.isInputInsufficient(res1, true)).isTrue();
            assertThat(target.isInputInsufficient(res2, true)).isTrue();
        }

        @Test
        @DisplayName("休日の場合、作業内容と稼働時間の両方がない、または両方ある場合はfalse(十分)を返すこと")
        void Holiday_BothAbsentOrBothPresent_ReturnsFalse() {
            GeminiParseResult res1 = GeminiParseResult.builder().logDate("2026-06-30").holiday(true).workHours(0)
                    .tasks("").build();
            GeminiParseResult res2 = GeminiParseResult.builder().logDate("2026-06-30").holiday(true).workHours(2.0)
                    .tasks("勉強").build();

            assertThat(target.isInputInsufficient(res1, true)).isFalse();
            assertThat(target.isInputInsufficient(res2, true)).isFalse();
        }
    }

    @Nested
    @DisplayName("不足項目メッセージの構築")
    class BuildMissingFieldsMessage {

        @Test
        @DisplayName("replyMessageが設定されている場合、それをそのまま返すこと")
        void ReplyMessage_Present_ReturnsDirectly() {
            GeminiParseResult result = GeminiParseResult.builder().replyMessage("対話をやり直してください。").build();
            assertThat(target.buildMissingFieldsMessage(result, false)).isEqualTo("対話をやり直してください。");
        }

        @Test
        @DisplayName("日付が空の場合は作業日を求めること")
        void LogDate_Blank_ReturnsLogDateRequired() {
            GeminiParseResult result = GeminiParseResult.builder().logDate(null).build();
            assertThat(target.buildMissingFieldsMessage(result, false)).contains("作業日");
        }

        @Test
        @DisplayName("平日の場合、不足している項目(稼働時間・作業内容)を求めること")
        void Weekday_MissingFields_ReturnsRequiredMessage() {
            GeminiParseResult res1 = GeminiParseResult.builder().logDate("2026-06-29").workHours(0).tasks("").build();
            GeminiParseResult res2 = GeminiParseResult.builder().logDate("2026-06-29").workHours(0).tasks("開発").build();

            assertThat(target.buildMissingFieldsMessage(res1, false)).contains("稼働時間と作業内容");
            assertThat(target.buildMissingFieldsMessage(res2, false)).contains("稼働時間");
        }

        @Test
        @DisplayName("休日の場合、作業内容があり稼働時間がない場合は稼働時間を求めること")
        void Holiday_HasTasksMissingHours_ReturnsHoursRequired() {
            GeminiParseResult result = GeminiParseResult.builder().logDate("2026-06-30").holiday(true).workHours(0)
                    .tasks("勉強").build();
            assertThat(target.buildMissingFieldsMessage(result, true)).contains("稼働時間");
        }
    }

    @Nested
    @DisplayName("カレンダーイベント登録")
    class RegisterCalendarEvent {

        @Test
        @DisplayName("カレンダーイベントが正常に登録・更新されること")
        void registerCalendarEvent_Success() {
            LocalDate date = LocalDate.of(2026, 6, 29);
            GeminiParseResult result = GeminiParseResult.builder().holiday(false).workHours(7.5).tasks("開発").diary("日記")
                    .sentiment(Sentiment.HAPPY).build();

            target.registerCalendarEvent("cal-id", date, result);

            verify(googleCalendarGateway).insertOrUpdateEvent(
                    eq("cal-id"),
                    eq(date),
                    contains("7.5"),
                    contains("[日報]"));
        }

        @Test
        @DisplayName("カレンダー登録時に例外が発生してもキャッチされ、エラーが伝播しないこと")
        void registerCalendarEvent_Fails_ExceptionCaught() {
            LocalDate date = LocalDate.of(2026, 6, 29);
            GeminiParseResult result = GeminiParseResult.builder().holiday(false).workHours(7.5).tasks("開発").build();

            doThrow(new RuntimeException("API error")).when(googleCalendarGateway)
                    .insertOrUpdateEvent(anyString(), any(LocalDate.class), anyString(), anyString());

            assertThatCode(() -> target.registerCalendarEvent("cal-id", date, result))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("日報の構築と保存")
    class SaveDailyLog {

        @Test
        @DisplayName("新規保存時、createdAtとupdatedAtが設定されて保存されること")
        void saveDailyLog_NewRecord() {
            LocalDate date = LocalDate.of(2026, 6, 29);
            GeminiParseResult result = GeminiParseResult.builder().holiday(false).workHours(8.0).tasks("開発").diary("日記")
                    .sentiment(Sentiment.HAPPY).build();

            when(dailyLogRepository.findByUserIdAndDate("user-id", date)).thenReturn(Optional.empty());

            Log savedLog = target.saveDailyLog("user-id", date, "raw text", result, "cal-id");

            assertThat(savedLog.getSlackUserId()).isEqualTo("user-id");
            assertThat(savedLog.getLogDate()).isEqualTo(date);
            assertThat(savedLog.getRawText()).isEqualTo("raw text");
            assertThat(savedLog.getTasks()).isEqualTo("開発");
            assertThat(savedLog.getWorkHours()).isEqualTo(8.0);
            assertThat(savedLog.getCreatedAt()).isNotNull();
            assertThat(savedLog.getUpdatedAt()).isNotNull();

            verify(dailyLogRepository).save(any(Log.class));
        }

        @Test
        @DisplayName("既存レコードの更新時、createdAtを引き継ぎ、updatedAtが更新されて保存されること")
        void saveDailyLog_ExistingRecord_Updates() {
            LocalDate date = LocalDate.of(2026, 6, 29);
            Instant originalCreatedAt = Instant.now().minusSeconds(100);
            Log existing = Log.builder().slackUserId("user-id").logDate(date).createdAt(originalCreatedAt).build();
            GeminiParseResult result = GeminiParseResult.builder().holiday(false).workHours(8.0).tasks("更新開発").build();

            when(dailyLogRepository.findByUserIdAndDate("user-id", date)).thenReturn(Optional.of(existing));

            Log savedLog = target.saveDailyLog("user-id", date, "new text", result, "cal-id");

            assertThat(savedLog.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(savedLog.getTasks()).isEqualTo("更新開発");
            verify(dailyLogRepository).save(any(Log.class));
        }
    }

    @Nested
    @DisplayName("月末日判定")
    class IsLastDayOfMonth {

        @Test
        @DisplayName("指定の日付が月末日である場合にtrueを返すこと")
        void isLastDayOfMonth_True() {
            assertThat(target.isLastDayOfMonth(LocalDate.of(2026, 6, 30))).isTrue();
            assertThat(target.isLastDayOfMonth(LocalDate.of(2026, 2, 28))).isTrue();
        }

        @Test
        @DisplayName("指定の日付が月末日でない場合にfalseを返すこと")
        void isLastDayOfMonth_False() {
            assertThat(target.isLastDayOfMonth(LocalDate.of(2026, 6, 29))).isFalse();
        }

        @Test
        @DisplayName("引数がnullの場合はfalseを返すこと")
        void isLastDayOfMonth_Null() {
            assertThat(target.isLastDayOfMonth(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("月間振り返りログデータフォーマット")
    class FormatReflectionLogs {

        @Test
        @DisplayName("月間ログリストが正しくテキスト整形されること")
        void formatReflectionLogs_Success() {
            Log l1 = Log.builder().logDate(LocalDate.of(2026, 6, 1)).tasks("作業A").diary("日記A").build();
            Log l2 = Log.builder().logDate(LocalDate.of(2026, 6, 2)).tasks("作業B").diary("").build();

            String result = target.formatReflectionLogs(List.of(l1, l2));

            assertThat(result).contains("Date: 2026-06-01")
                    .contains("Tasks: 作業A")
                    .contains("Diary: 日記A")
                    .contains("Date: 2026-06-02")
                    .contains("Tasks: 作業B")
                    .contains("Diary: ");
        }

        @Test
        @DisplayName("引数がnullの場合は空文字が返ること")
        void formatReflectionLogs_Null() {
            assertThat(target.formatReflectionLogs(null)).isEmpty();
        }
    }
}
