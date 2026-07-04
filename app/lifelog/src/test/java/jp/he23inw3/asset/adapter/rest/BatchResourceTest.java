package jp.he23inw3.asset.adapter.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import jp.he23inw3.asset.adapter.dto.BatchExecutionHistoryListResponse;
import jp.he23inw3.asset.adapter.dto.BatchExecutionHistoryQueryRequest;
import jp.he23inw3.asset.adapter.mapper.BatchExecutionHistoryMapper;
import jp.he23inw3.asset.domain.exception.InvalidRequestException;
import jp.he23inw3.asset.domain.model.BatchExecutionHistory;
import jp.he23inw3.asset.domain.model.BatchStatus;
import jp.he23inw3.asset.domain.repository.dto.BatchExecutionHistoryQuery;
import jp.he23inw3.asset.usecase.BatchExecutionHistoryUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BatchResourceTest {

    @Mock
    BatchExecutionHistoryUseCase batchHistoryUseCase;

    @Mock
    BatchExecutionHistoryMapper batchHistoryMapper;

    @InjectMocks
    BatchResource target;

    // =========================================================================
    // バッチ実行履歴一覧 (BE-API403)
    // =========================================================================
    @Nested
    @DisplayName("バッチ実行履歴一覧取得")
    class GetBatchHistories {

        @Test
        @DisplayName("バッチ実行履歴の一覧取得が正常に行われること")
        void getBatchHistories_Success() {
            BatchExecutionHistoryQueryRequest request = new BatchExecutionHistoryQueryRequest();
            request.setLimit(10);
            request.setOffset(5);
            request.setStart("2026-06-01");
            request.setEnd("2026-06-30");
            request.setBatchId("test-batch");
            request.setStatus("SUCCESS");

            List<BatchExecutionHistory> mockHistories = Collections
                    .singletonList(BatchExecutionHistory.builder().id("1").build());
            List<BatchExecutionHistoryListResponse.BatchExecutionHistory> mockResponses = Collections
                    .singletonList(BatchExecutionHistoryListResponse.BatchExecutionHistory.builder().id("1").build());

            ArgumentCaptor<BatchExecutionHistoryQuery> captor = ArgumentCaptor
                    .forClass(BatchExecutionHistoryQuery.class);
            when(batchHistoryUseCase.getHistories(captor.capture())).thenReturn(mockHistories);
            when(batchHistoryMapper.toResponseHistoryList(mockHistories)).thenReturn(mockResponses);

            BatchExecutionHistoryListResponse responses = target.getBatchHistories(request);

            assertThat(responses).isNotNull();
            assertThat(responses.getTotalSize()).isEqualTo(1);
            assertThat(responses.getBatchExecutionHistories()).hasSize(1);
            verify(batchHistoryUseCase).getHistories(any());
            verify(batchHistoryMapper).toResponseHistoryList(mockHistories);

            BatchExecutionHistoryQuery query = captor.getValue();
            assertThat(query.getLimit()).isEqualTo(10);
            assertThat(query.getOffset()).isEqualTo(5);
            assertThat(query.getStart()).isEqualTo(LocalDateTime.of(2026, 6, 1, 0, 0, 0));
            assertThat(query.getEnd()).isEqualTo(LocalDateTime.of(2026, 6, 30, 23, 59, 59));
            assertThat(query.getBatchId()).isEqualTo("test-batch");
            assertThat(query.getStatus()).isEqualTo(BatchStatus.SUCCESS);
        }

        @Test
        @DisplayName("バッチ履歴取得で日付フォーマットエラー時にInvalidRequestExceptionとなること")
        void getBatchHistories_InvalidDate_BadRequest() {
            BatchExecutionHistoryQueryRequest request = new BatchExecutionHistoryQueryRequest();
            request.setStart("invalid-date");

            assertThatThrownBy(() -> target.getBatchHistories(request)).isInstanceOf(InvalidRequestException.class);
        }

        @Test
        @DisplayName("バッチ履歴取得中に予期しない例外が発生した場合にそのままスローされること")
        void getBatchHistories_Exception_ThrowsException() {
            BatchExecutionHistoryQueryRequest request = new BatchExecutionHistoryQueryRequest();

            when(batchHistoryUseCase.getHistories(any())).thenThrow(new RuntimeException("Database error"));

            assertThatThrownBy(() -> target.getBatchHistories(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database error");
        }
    }
}
