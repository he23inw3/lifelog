package jp.he23inw3.asset.configuration;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

@ApplicationScoped
@Alternative
@Priority(1)
@IfBuildProfile("dev")
public class DevGoogleCalendarProducer {

    /**
     * dev プロファイル時に優先してインジェクションされる Calendar インスタンスを生成します。
     * 
     * @return Calendar エミュレータ接続用クライアント
     */
    @Produces
    @Singleton
    public Calendar calendar() {
        try {
            Calendar mockCalendar = mock(Calendar.class);
            Calendar.CalendarList mockList = mock(Calendar.CalendarList.class);
            Calendar.CalendarList.List mockListRequest = mock(Calendar.CalendarList.List.class);

            when(mockCalendar.calendarList()).thenReturn(mockList);
            when(mockList.list()).thenReturn(mockListRequest);
            when(mockListRequest.setMaxResults(anyInt())).thenReturn(mockListRequest);
            when(mockListRequest.execute()).thenReturn(new CalendarList());
            return mockCalendar;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize mock Google Calendar client", e);
        }
    }
}
