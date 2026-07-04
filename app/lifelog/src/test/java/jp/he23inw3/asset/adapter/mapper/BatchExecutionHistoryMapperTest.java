package jp.he23inw3.asset.adapter.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jp.he23inw3.asset.adapter.dto.BatchExecutionDetailHistoryResponse;
import jp.he23inw3.asset.adapter.dto.BatchExecutionHistoryListResponse;
import jp.he23inw3.asset.domain.model.BatchExecutionHistory;
import jp.he23inw3.asset.domain.model.BatchStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class BatchExecutionHistoryMapperTest {

    private final BatchExecutionHistoryMapper mapper = Mappers.getMapper(BatchExecutionHistoryMapper.class);

    @Nested
    @DisplayName("BatchExecutionHistoryからBatchExecutionHistoryListResponseへの変換")
    class ToResponse {

        @Test
        @DisplayName("基本フィールドが正しくマッピングされること")
        void toResponse_ShouldMapBasicFields() {
            // Arrange
            LocalDateTime startedAt = LocalDateTime.of(2026, 6, 9, 10, 0);
            LocalDateTime finishedAt = LocalDateTime.of(2026, 6, 9, 10, 5);
            BatchExecutionHistory history = BatchExecutionHistory.builder()
                    .id("history-id-123")
                    .batchName("test-batch")
                    .startedAt(startedAt)
                    .finishedAt(finishedAt)
                    .status(BatchStatus.SUCCESS)
                    .errorMessage(null)
                    .errorStackTrace(null)
                    .traceId("trace-id-abc")
                    .build();

            // Act
            BatchExecutionHistoryListResponse.BatchExecutionHistory response = mapper.toResponseHistory(history);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("history-id-123");
            assertThat(response.getBatchName()).isEqualTo("test-batch");
            assertThat(response.getStartedAt()).isEqualTo(startedAt);
            assertThat(response.getFinishedAt()).isEqualTo(finishedAt);
            assertThat(response.getStatus()).isEqualTo("SUCCESS");
        }

        @Test
        @DisplayName("nullを渡した場合はnullが返ること")
        void toResponse_WithNull_ShouldReturnNull() {
            assertThat(mapper.toResponseHistory(null)).isNull();
        }
    }

    @Nested
    @DisplayName("BatchExecutionHistoryからBatchExecutionDetailHistoryResponseへの変換")
    class ToDetailResponseHistory {

        @Test
        @DisplayName("すべてのフィールドが正しくマッピングされること")
        void toDetailResponse_ShouldMapAllFields() {
            // Arrange
            LocalDateTime startedAt = LocalDateTime.of(2026, 6, 9, 10, 0);
            LocalDateTime finishedAt = LocalDateTime.of(2026, 6, 9, 10, 5);
            BatchExecutionHistory history = BatchExecutionHistory.builder()
                    .id("history-id-123")
                    .batchName("test-batch")
                    .startedAt(startedAt)
                    .finishedAt(finishedAt)
                    .status(BatchStatus.SUCCESS)
                    .errorMessage(null)
                    .errorStackTrace(null)
                    .traceId("trace-id-abc")
                    .build();

            // Act
            BatchExecutionDetailHistoryResponse response = mapper.toDetailResponseHistory(history);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("history-id-123");
            assertThat(response.getBatchName()).isEqualTo("test-batch");
            assertThat(response.getStartedAt()).isEqualTo(startedAt);
            assertThat(response.getFinishedAt()).isEqualTo(finishedAt);
            assertThat(response.getStatus()).isEqualTo("SUCCESS");
            assertThat(response.getErrorMessage()).isNull();
            assertThat(response.getErrorStackTrace()).isNull();
            assertThat(response.getTraceId()).isEqualTo("trace-id-abc");
        }

        @Test
        @DisplayName("nullを渡した場合はnullが返ること")
        void toDetailResponse_WithNull_ShouldReturnNull() {
            assertThat(mapper.toDetailResponseHistory(null)).isNull();
        }
    }

    @Nested
    @DisplayName("BatchExecutionHistoryのリストからBatchExecutionHistoryListResponseのリストへの変換")
    class ToResponseList {

        @Test
        @DisplayName("リストの要素すべてが正しくマッピングされること")
        void toResponseList_ShouldMapList() {
            // Arrange
            BatchExecutionHistory history1 = BatchExecutionHistory.builder().id("1").status(BatchStatus.SUCCESS)
                    .build();
            BatchExecutionHistory history2 = BatchExecutionHistory.builder().id("2").status(BatchStatus.FAILED).build();

            // Act
            List<BatchExecutionHistoryListResponse.BatchExecutionHistory> responses = mapper
                    .toResponseHistoryList(Arrays.asList(history1, history2));

            // Assert
            assertThat(responses).isNotNull().hasSize(2);
            assertThat(responses.get(0).getId()).isEqualTo("1");
            assertThat(responses.get(0).getStatus()).isEqualTo("SUCCESS");
            assertThat(responses.get(1).getId()).isEqualTo("2");
            assertThat(responses.get(1).getStatus()).isEqualTo("FAILED");
        }

        @Test
        @DisplayName("nullを渡した場合はnullが返ること")
        void toResponseList_WithNull_ShouldReturnNull() {
            assertThat(mapper.toResponseHistoryList(null)).isNull();
        }

        @Test
        @DisplayName("空のリストを渡した場合は空のリストが返ること")
        void toResponseList_WithEmptyList_ShouldReturnEmptyList() {
            List<BatchExecutionHistoryListResponse.BatchExecutionHistory> responses = mapper
                    .toResponseHistoryList(Collections.emptyList());
            assertThat(responses).isNotNull().isEmpty();
        }
    }
}
