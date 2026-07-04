package jp.he23inw3.asset.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.model.BatchExecutionHistory;
import jp.he23inw3.asset.domain.repository.BatchExecutionHistoryRepository;
import jp.he23inw3.asset.domain.repository.dto.BatchExecutionHistoryQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BatchExecutionHistoryUseCaseTest {

    @Mock
    BatchExecutionHistoryRepository historyRepository;

    @InjectMocks
    BatchExecutionHistoryUseCase target;

    @Nested
    @DisplayName("バッチ実行履歴一覧取得")
    class GetHistories {

        @Test
        @DisplayName("検索条件に対応するバッチの実行履歴一覧を取得すること")
        void getHistories_Success() {
            // Arrange
            BatchExecutionHistoryQuery query = BatchExecutionHistoryQuery.builder().batchId("BE-BATCH001").build();
            List<BatchExecutionHistory> expected = Collections
                    .singletonList(BatchExecutionHistory.builder().id("123").batchName("BE-BATCH001").build());
            when(historyRepository.findByQuery(query)).thenReturn(expected);

            // Act
            List<BatchExecutionHistory> actual = target.getHistories(query);

            // Assert
            assertThat(actual).isEqualTo(expected);
            verify(historyRepository).findByQuery(query);
        }
    }

    @Nested
    @DisplayName("バッチ実行履歴詳細取得")
    class GetHistory {

        @Test
        @DisplayName("履歴IDに紐づくバッチ実行履歴詳細を取得すること - 成功")
        void getHistory_Success() {
            // Arrange
            String id = "123";
            BatchExecutionHistory expected = BatchExecutionHistory.builder().id(id).batchName("BE-BATCH001").build();
            when(historyRepository.findById(id)).thenReturn(Optional.of(expected));

            // Act
            BatchExecutionHistory actual = target.getHistory(id);

            // Assert
            assertThat(actual).isEqualTo(expected);
            verify(historyRepository).findById(id);
        }

        @Test
        @DisplayName("履歴IDに対応する詳細が存在しない場合にResourceNotFoundExceptionをスローすること")
        void getHistory_NotFound_ThrowsResourceNotFoundException() {
            // Arrange
            String id = "nonexistent";
            when(historyRepository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> target.getHistory(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Batch history not found for " + id);
            verify(historyRepository).findById(id);
        }
    }
}
