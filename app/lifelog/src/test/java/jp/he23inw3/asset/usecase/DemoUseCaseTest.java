package jp.he23inw3.asset.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.exception.DemoModeDisabledException;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.model.DemoCalendarEvent;
import jp.he23inw3.asset.domain.model.DemoMessage;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.DemoCalendarRepository;
import jp.he23inw3.asset.domain.repository.DemoMessageRepository;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemoUseCaseTest {

    @Mock
    LifeLogConfig config;

    @Mock
    LifeLogConfig.Demo demoConfig;

    @Mock
    UserSettingRepository userSettingRepository;

    @Mock
    DemoCalendarRepository demoCalendarRepository;

    @Mock
    DemoMessageRepository demoMessageRepository;

    @InjectMocks
    DemoUseCase target;

    @BeforeEach
    void setUp() {
        when(config.demo()).thenReturn(demoConfig);
    }

    @Nested
    @DisplayName("デモ用カレンダーイベント一覧取得")
    class GetDemoCalendar {

        @Test
        @DisplayName("デモモードが無効な場合にDemoModeDisabledExceptionをスローすること")
        void testGetDemoCalendar_DemoDisabled_ThrowsException() {
            when(demoConfig.enabled()).thenReturn(false);

            assertThatThrownBy(() -> target.getDemoCalendar(YearMonth.of(2026, 6)))
                    .isInstanceOf(DemoModeDisabledException.class)
                    .hasMessageContaining("デモモードが無効です");
        }

        @Test
        @DisplayName("デモユーザーの設定が見つからない場合にResourceNotFoundExceptionをスローすること")
        void testGetDemoCalendar_UserNotFound_ThrowsException() {
            when(demoConfig.enabled()).thenReturn(true);
            when(demoConfig.userEmail()).thenReturn("demo@example.com");
            when(userSettingRepository.findByEmail("demo@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> target.getDemoCalendar(YearMonth.of(2026, 6)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("デモユーザー設定が見つかりません");
        }

        @Test
        @DisplayName("カレンダーイベント一覧を正常に取得できること")
        void testGetDemoCalendar_Success() throws Exception {
            when(demoConfig.enabled()).thenReturn(true);
            when(demoConfig.userEmail()).thenReturn("demo@example.com");

            UserSetting setting = UserSetting.builder()
                    .slackUserId("U123")
                    .googleCalendarId("cal-123")
                    .build();
            when(userSettingRepository.findByEmail("demo@example.com")).thenReturn(Optional.of(setting));

            DemoCalendarEvent eventModel = DemoCalendarEvent.builder()
                    .calendarId("cal-123")
                    .date("2026-06-15")
                    .title("Demo Event")
                    .description("Desc")
                    .holiday(true)
                    .syncedAt(LocalDateTime.of(2026, 6, 14, 12, 0))
                    .build();

            when(demoCalendarRepository.findByCalendarIdAndPeriod(eq("cal-123"), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(Collections.singletonList(eventModel));

            List<DemoCalendarEvent> result = target.getDemoCalendar(YearMonth.of(2026, 6));

            assertThat(result).hasSize(1);
            DemoCalendarEvent event = result.get(0);
            assertThat(event.getCalendarId()).isEqualTo("cal-123");
            assertThat(event.getDate()).isEqualTo("2026-06-15");
            assertThat(event.getTitle()).isEqualTo("Demo Event");
            assertThat(event.isHoliday()).isTrue();
        }
    }

    @Nested
    @DisplayName("デモ用メッセージ一覧取得")
    class GetDemoMessages {

        @Test
        @DisplayName("デモモードが無効な場合にDemoModeDisabledExceptionをスローすること")
        void testGetDemoMessages_DemoDisabled_ThrowsException() {
            when(demoConfig.enabled()).thenReturn(false);

            assertThatThrownBy(() -> target.getDemoMessages("user-id"))
                    .isInstanceOf(DemoModeDisabledException.class)
                    .hasMessageContaining("デモモードが無効です");
        }

        @Test
        @DisplayName("デモ用メッセージ一覧を正常に取得できること")
        void testGetDemoMessages_Success() throws Exception {
            when(demoConfig.enabled()).thenReturn(true);

            DemoMessage messageModel = DemoMessage.builder()
                    .slackUserId("user-123")
                    .type("POST")
                    .text("Hello Demo")
                    .timestamp(LocalDateTime.of(2026, 6, 14, 12, 0))
                    .build();

            when(demoMessageRepository.findBySlackUserId("user-123"))
                    .thenReturn(Collections.singletonList(messageModel));

            List<DemoMessage> result = target.getDemoMessages("user-123");

            assertThat(result).hasSize(1);
            DemoMessage message = result.get(0);
            assertThat(message.getSlackUserId()).isEqualTo("user-123");
            assertThat(message.getType()).isEqualTo("POST");
            assertThat(message.getText()).isEqualTo("Hello Demo");
        }
    }
}
