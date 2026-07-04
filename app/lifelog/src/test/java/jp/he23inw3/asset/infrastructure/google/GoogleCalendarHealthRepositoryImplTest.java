package jp.he23inw3.asset.infrastructure.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.google.api.services.calendar.Calendar;
import jp.he23inw3.asset.domain.model.HealthStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleCalendarHealthRepositoryImplTest {

    @Mock
    Calendar calendarService;

    @InjectMocks
    GoogleCalendarHealthRepositoryImpl target;

    @Nested
    @DisplayName("ヘルスチェック")
    class CheckHealth {

        @Test
        @DisplayName("Google Calendar APIの疎通チェック正常時、UPを返すこと")
        void checkHealth_Success_ShouldReturnUp() throws Exception {
            // Arrange
            Calendar.CalendarList calendarList = mock(Calendar.CalendarList.class);
            Calendar.CalendarList.List listRequest = mock(Calendar.CalendarList.List.class);

            when(calendarService.calendarList()).thenReturn(calendarList);
            when(calendarList.list()).thenReturn(listRequest);
            when(listRequest.setMaxResults(1)).thenReturn(listRequest);

            // Act
            HealthStatus actual = target.checkHealth();

            // Assert
            assertThat(actual).isEqualTo(HealthStatus.UP);
            assertThat(target.getServiceName()).isEqualTo("Google Calendar");
            verify(listRequest).execute();
        }

        @Test
        @DisplayName("Google Calendar APIの疎通チェック異常時、DOWNを返すこと")
        void checkHealth_Failure_ShouldReturnDown() throws Exception {
            // Arrange
            Calendar.CalendarList calendarList = mock(Calendar.CalendarList.class);
            Calendar.CalendarList.List listRequest = mock(Calendar.CalendarList.List.class);

            when(calendarService.calendarList()).thenReturn(calendarList);
            when(calendarList.list()).thenReturn(listRequest);
            when(listRequest.setMaxResults(1)).thenReturn(listRequest);
            doThrow(new RuntimeException("API error")).when(listRequest).execute();

            // Act
            HealthStatus actual = target.checkHealth();

            // Assert
            assertThat(actual).isEqualTo(HealthStatus.DOWN);
        }
    }
}
