package jp.he23inw3.asset.adapter.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.YearMonth;
import java.util.List;
import jp.he23inw3.asset.adapter.dto.DemoCalendarListResponse;
import jp.he23inw3.asset.adapter.dto.DemoMessageListResponse;
import jp.he23inw3.asset.domain.exception.InvalidDemoParameterException;
import jp.he23inw3.asset.usecase.DemoUseCase;
import jp.he23inw3.asset.domain.model.DemoCalendarEvent;
import jp.he23inw3.asset.domain.model.DemoMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jp.he23inw3.asset.adapter.mapper.DemoMapper;

@ExtendWith(MockitoExtension.class)
class DemoResourceTest {

    @Mock
    DemoUseCase demoUseCase;

    @Mock
    DemoMapper demoMapper;

    @InjectMocks
    DemoResource target;

    @Test
    @DisplayName("カレンダー取得時に月パラメータが正しい場合は正常終了すること")
    void getDemoCalendar_Success() {
        String month = "2024-06";
        List<DemoCalendarEvent> mockResponses = List.of(mock(DemoCalendarEvent.class));
        List<DemoCalendarListResponse.CalendarEvent> adapterResponses = List.of(mock(DemoCalendarListResponse.CalendarEvent.class));

        when(demoUseCase.getDemoCalendar(YearMonth.parse(month))).thenReturn(mockResponses);
        when(demoMapper.toCalendarEventsResponse(mockResponses)).thenReturn(adapterResponses);

        DemoCalendarListResponse response = target.getDemoCalendar(month);

        assertThat(response).isNotNull();
        assertThat(response.getTotalSize()).isEqualTo(1);
        assertThat(response.getCalendarEvents()).hasSize(1);
        verify(demoUseCase).getDemoCalendar(YearMonth.parse(month));
        verify(demoMapper).toCalendarEventsResponse(mockResponses);
    }

    @Test
    @DisplayName("カレンダー取得時に月パラメータの形式が不正な場合はInvalidDemoParameterExceptionをスローすること")
    void getDemoCalendar_InvalidFormat_ThrowsException() {
        String month = "2024/06";

        InvalidDemoParameterException ex = org.junit.jupiter.api.Assertions.assertThrows(
                InvalidDemoParameterException.class,
                () -> target.getDemoCalendar(month));

        assertThat(ex.getMessage()).isEqualTo("月フォーマットが正しくありません。YYYY-MM形式で指定してください。");
        verifyNoInteractions(demoUseCase);
    }

    @Test
    @DisplayName("Slackメッセージ取得時に正常にDemoMessagesResponseが取得できること")
    void getDemoMessages_Success() {
        String slackUserId = "U123456";
        List<DemoMessage> mockResponses = List.of(mock(DemoMessage.class));
        List<DemoMessageListResponse.DemoMessage> adapterResponses = List.of(mock(DemoMessageListResponse.DemoMessage.class));

        when(demoUseCase.getDemoMessages(slackUserId)).thenReturn(mockResponses);
        when(demoMapper.toDemoMessagesResponse(mockResponses)).thenReturn(adapterResponses);

        DemoMessageListResponse response = target.getDemoMessages(slackUserId);

        assertThat(response).isNotNull();
        assertThat(response.getTotalSize()).isEqualTo(1);
        assertThat(response.getMessages()).hasSize(1);
        verify(demoUseCase).getDemoMessages(slackUserId);
        verify(demoMapper).toDemoMessagesResponse(mockResponses);
    }
}
