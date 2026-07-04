package jp.he23inw3.asset.adapter.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import jp.he23inw3.asset.adapter.dto.LogCalendarSyncResponse;
import jp.he23inw3.asset.adapter.dto.LogDetailResponse;
import jp.he23inw3.asset.adapter.dto.LogListResponse;
import jp.he23inw3.asset.adapter.dto.LogSearchQueryRequest;
import jp.he23inw3.asset.adapter.mapper.LogCalendarSyncMapper;
import jp.he23inw3.asset.adapter.mapper.LogMapper;
import jp.he23inw3.asset.domain.exception.InvalidRequestException;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.model.Sentiment;
import jp.he23inw3.asset.usecase.AdminLogUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogResourceTest {

    @Mock
    AdminLogUseCase adminLogUseCase;

    @Mock
    LogMapper logMapper;

    @Mock
    LogCalendarSyncMapper logCalendarSyncMapper;

    @InjectMocks
    LogResource target;

    // =========================================================================
    // 日報検索 (BE-API406)
    // =========================================================================
    @Nested
    @DisplayName("日報検索")
    class SearchLogs {

        @Test
        @DisplayName("日報検索が正常に行われること")
        void searchLogs_Success() {
            List<Log> logs = Collections.singletonList(Log.builder().slackUserId("U123").build());
            List<LogListResponse.Log> responses = Collections
                    .singletonList(LogListResponse.Log.builder().build());

            LogSearchQueryRequest request = new LogSearchQueryRequest();
            request.setUser("U123");
            request.setFrom("2026-06-01");
            request.setTo("2026-06-30");
            request.setHoliday(true);
            request.setSentiment("POSITIVE");

            when(adminLogUseCase.searchLogs("U123", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), true, Sentiment.NEUTRAL)).thenReturn(logs);
            when(logMapper.toListResponseLogList(logs)).thenReturn(responses);

            LogListResponse result = target.searchLogs(request);

            assertThat(result).isNotNull();
            assertThat(result.getTotalSize()).isEqualTo(1);
            assertThat(result.getLogs()).hasSize(1);
            verify(adminLogUseCase).searchLogs("U123", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), true, Sentiment.NEUTRAL);
            verify(logMapper).toListResponseLogList(logs);
        }

        @Test
        @DisplayName("日報検索で日付フォーマットエラー時にInvalidRequestExceptionとなること")
        void searchLogs_InvalidDate_BadRequest() {
            LogSearchQueryRequest request = new LogSearchQueryRequest();
            request.setUser("U123");
            request.setFrom("invalid-date");
            request.setTo("2026-06-30");
            request.setHoliday(true);
            request.setSentiment("POSITIVE");

            assertThatThrownBy(() -> target.searchLogs(request))
                    .isInstanceOf(InvalidRequestException.class);
        }
    }

    // =========================================================================
    // 日報詳細取得 (BE-API407)
    // =========================================================================
    @Nested
    @DisplayName("日報詳細取得")
    class GetLogDetail {

        @Test
        @DisplayName("日報詳細が正常に取得できること")
        void getLogDetail_Success() {
            Log logObj = Log.builder()
                    .slackUserId("U123")
                    .logDate(LocalDate.parse("2026-06-11"))
                    .build();
            LogDetailResponse responseObj = LogDetailResponse.builder()
                    .slackUserId("U123")
                    .build();

            when(adminLogUseCase.getLogDetail("U123", LocalDate.parse("2026-06-11"))).thenReturn(logObj);
            when(logMapper.toDetailResponse(logObj)).thenReturn(responseObj);

            LogDetailResponse result = target.getLogDetail("U123", "2026-06-11");

            assertThat(result).isNotNull();
            assertThat(result.getSlackUserId()).isEqualTo("U123");
            verify(adminLogUseCase).getLogDetail("U123", LocalDate.parse("2026-06-11"));
            verify(logMapper).toDetailResponse(logObj);
        }

        @Test
        @DisplayName("日報詳細取得で日付フォーマットエラー時にInvalidRequestExceptionとなること")
        void getLogDetail_InvalidDate_BadRequest() {
            assertThatThrownBy(() -> target.getLogDetail("U123", "invalid-date"))
                    .isInstanceOf(InvalidRequestException.class);
        }

        @Test
        @DisplayName("対象の日報が存在しない場合にResourceNotFoundExceptionがスローされること")
        void getLogDetail_NotFound_ThrowsResourceNotFoundException() {
            LocalDate date = LocalDate.parse("2026-06-11");
            when(adminLogUseCase.getLogDetail("U123", date)).thenThrow(new ResourceNotFoundException("Not found"));

            assertThatThrownBy(() -> target.getLogDetail("U123", "2026-06-11"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================
    // Google Calendar 再同期 (BE-API408)
    // =========================================================================
    @Nested
    @DisplayName("Google Calendar再同期")
    class SyncCalendar {

        @Test
        @DisplayName("Google Calendar再同期が正常に行われること")
        void syncCalendar_Success() {
            LogCalendarSyncResponse mockResponse = LogCalendarSyncResponse.builder().message("Calendar synced successfully.").build();
            when(logCalendarSyncMapper.toResponse("Calendar synced successfully.")).thenReturn(mockResponse);

            LogCalendarSyncResponse response = target.syncCalendar("U123", "2026-06-11");

            assertThat(response).isEqualTo(mockResponse);
            verify(adminLogUseCase).syncCalendar("U123", LocalDate.parse("2026-06-11"));
        }

        @Test
        @DisplayName("Google Calendar再同期で日付フォーマットエラー時にInvalidRequestExceptionとなること")
        void syncCalendar_InvalidDate_BadRequest() {
            assertThatThrownBy(() -> target.syncCalendar("U123", "invalid-date"))
                    .isInstanceOf(InvalidRequestException.class);
        }
    }
}
