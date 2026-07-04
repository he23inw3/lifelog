package jp.he23inw3.asset.infrastructure.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.gateway.CryptoGateway;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleCalendarGatewayImplTest {

    @Mock
    LifeLogConfig config;

    @Mock
    LifeLogConfig.Demo demoConfig;

    @Mock
    LifeLogConfig.Google googleConfig;

    @Mock
    UserSettingRepository userSettingRepository;

    @Mock
    CryptoGateway cryptoGateway;

    @Mock
    Calendar calendar;

    @Mock
    Calendar.Events calendarEvents;

    @Mock
    Calendar.Events.List calendarEventsList;

    @Mock
    Calendar.Events.Insert calendarEventsInsert;

    @Mock
    Calendar.Events.Update calendarEventsUpdate;

    @Mock
    Events events;

    GoogleCalendarGatewayImpl target;

    @BeforeEach
    void setUp() throws Exception {
        when(config.demo()).thenReturn(demoConfig);
        when(demoConfig.enabled()).thenReturn(true); // デモモードを有効にして defaultCalendarService が使われるようにする

        target = new GoogleCalendarGatewayImpl(config, userSettingRepository, cryptoGateway);

        // Reflectionでモックの Calendar を注入
        Field field = GoogleCalendarGatewayImpl.class.getDeclaredField("defaultCalendarService");
        field.setAccessible(true);
        field.set(target, calendar);
    }

    @Nested
    @DisplayName("isHolidayOrPaidLeaveメソッドのテスト")
    class IsHolidayOrPaidLeave {

        @Test
        @DisplayName("祝日カレンダーにイベントがある場合、有給・休暇・祝日(true)と判定されること")
        void testIsHoliday_True() throws Exception {
            String calendarId = "user@example.com";
            LocalDate date = LocalDate.of(2026, 6, 30);

            when(config.google()).thenReturn(googleConfig);
            when(googleConfig.japaneseHolidayCalendarId()).thenReturn("holiday_cal");

            when(calendar.events()).thenReturn(calendarEvents);
            when(calendarEvents.list(anyString())).thenReturn(calendarEventsList);
            when(calendarEventsList.setTimeMin(any())).thenReturn(calendarEventsList);
            when(calendarEventsList.setTimeMax(any())).thenReturn(calendarEventsList);
            when(calendarEventsList.setSingleEvents(anyBoolean())).thenReturn(calendarEventsList);
            when(calendarEventsList.setOrderBy(anyString())).thenReturn(calendarEventsList);
            when(calendarEventsList.setPageToken(any())).thenReturn(calendarEventsList);
            when(calendarEventsList.execute()).thenReturn(events);

            Event holidayEvent = new Event().setSummary("元日");
            when(events.getItems()).thenReturn(List.of(holidayEvent));

            boolean result = target.isHolidayOrPaidLeave(calendarId, date);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("ユーザーカレンダーに休暇イベントがある場合、有給・休暇・祝日(true)と判定されること")
        void testIsPaidLeave_True() throws Exception {
            String calendarId = "user@example.com";
            LocalDate date = LocalDate.of(2026, 6, 30);

            when(config.google()).thenReturn(googleConfig);
            when(googleConfig.japaneseHolidayCalendarId()).thenReturn("holiday_cal");

            when(calendar.events()).thenReturn(calendarEvents);
            when(calendarEvents.list(anyString())).thenReturn(calendarEventsList);
            when(calendarEventsList.setTimeMin(any())).thenReturn(calendarEventsList);
            when(calendarEventsList.setTimeMax(any())).thenReturn(calendarEventsList);
            when(calendarEventsList.setSingleEvents(anyBoolean())).thenReturn(calendarEventsList);
            when(calendarEventsList.setOrderBy(anyString())).thenReturn(calendarEventsList);
            when(calendarEventsList.setPageToken(any())).thenReturn(calendarEventsList);
            when(calendarEventsList.execute()).thenReturn(events);
            when(events.getNextPageToken()).thenReturn(null); // ページネーション終端

            // 1回目：祝日カレンダーのチェック（イベントなし）
            // 2回目：ユーザーカレンダーのチェック（「有休」イベントあり）
            Event leaveEvent = new Event().setSummary("有休を取ります");
            when(events.getItems()).thenReturn(List.of(), List.of(leaveEvent));

            boolean result = target.isHolidayOrPaidLeave(calendarId, date);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("祝日でも休暇でもない場合、falseと判定されること")
        void testNormalDay() throws Exception {
            String calendarId = "user@example.com";
            LocalDate date = LocalDate.of(2026, 6, 30);

            when(config.google()).thenReturn(googleConfig);
            when(googleConfig.japaneseHolidayCalendarId()).thenReturn("holiday_cal");

            when(calendar.events()).thenReturn(calendarEvents);
            when(calendarEvents.list(anyString())).thenReturn(calendarEventsList);
            when(calendarEventsList.setTimeMin(any())).thenReturn(calendarEventsList);
            when(calendarEventsList.setTimeMax(any())).thenReturn(calendarEventsList);
            when(calendarEventsList.setSingleEvents(anyBoolean())).thenReturn(calendarEventsList);
            when(calendarEventsList.setOrderBy(anyString())).thenReturn(calendarEventsList);
            when(calendarEventsList.setPageToken(any())).thenReturn(calendarEventsList);
            when(calendarEventsList.execute()).thenReturn(events);
            when(events.getNextPageToken()).thenReturn(null); // ページネーション終端

            when(events.getItems()).thenReturn(List.of());

            boolean result = target.isHolidayOrPaidLeave(calendarId, date);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("insertOrUpdateEventメソッドのテスト")
    class InsertOrUpdateEvent {

        @Test
        @DisplayName("既存の日報予定がない場合、新規挿入が実行されること")
        void testInsert() throws Exception {
            String calendarId = "user@example.com";
            LocalDate date = LocalDate.of(2026, 6, 30);

            when(calendar.events()).thenReturn(calendarEvents);
            when(calendarEvents.list(calendarId)).thenReturn(calendarEventsList);
            when(calendarEventsList.setTimeMin(any())).thenReturn(calendarEventsList);
            when(calendarEventsList.setTimeMax(any())).thenReturn(calendarEventsList);
            when(calendarEventsList.setSingleEvents(anyBoolean())).thenReturn(calendarEventsList);
            when(calendarEventsList.execute()).thenReturn(events);

            when(events.getItems()).thenReturn(List.of()); // 既存なし
            when(calendarEvents.insert(eq(calendarId), any(Event.class))).thenReturn(calendarEventsInsert);

            target.insertOrUpdateEvent(calendarId, date, "[日報]テスト", "内容");

            verify(calendarEventsInsert).execute();
        }

        @Test
        @DisplayName("既存の日報予定がある場合、更新が実行されること")
        void testUpdate() throws Exception {
            String calendarId = "user@example.com";
            LocalDate date = LocalDate.of(2026, 6, 30);

            when(calendar.events()).thenReturn(calendarEvents);
            when(calendarEvents.list(calendarId)).thenReturn(calendarEventsList);
            when(calendarEventsList.setTimeMin(any())).thenReturn(calendarEventsList);
            when(calendarEventsList.setTimeMax(any())).thenReturn(calendarEventsList);
            when(calendarEventsList.setSingleEvents(anyBoolean())).thenReturn(calendarEventsList);
            when(calendarEventsList.execute()).thenReturn(events);

            Event existing = new Event().setId("evt-123").setSummary("[日報]古い日報");
            when(events.getItems()).thenReturn(List.of(existing));
            when(calendarEvents.update(eq(calendarId), eq("evt-123"), any(Event.class)))
                    .thenReturn(calendarEventsUpdate);

            target.insertOrUpdateEvent(calendarId, date, "[日報]新しい日報", "内容");

            verify(calendarEventsUpdate).execute();
        }
    }
}
