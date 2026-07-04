package jp.he23inw3.asset.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;
import java.util.ArrayList;
import java.util.List;
import jp.he23inw3.asset.domain.model.BatchExecutionHistory;
import jp.he23inw3.asset.domain.model.BatchStatus;
import jp.he23inw3.asset.domain.repository.BatchExecutionHistoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BatchExecutionServiceTest {

    @Mock
    BatchExecutionHistoryRepository historyRepository;

    @InjectMocks
    BatchExecutionService target;

    @SuppressWarnings({"rawtypes"})
    private MockedStatic<CDI> mockedCdi;

    @BeforeEach
    void setUp() {
        mockedCdi = mockStatic(CDI.class);
    }

    @AfterEach
    void tearDown() {
        mockedCdi.close();
    }

    @Test
    @DisplayName("バッチが正常終了した際に、履歴がSUCCESSで保存されること")
    @SuppressWarnings("unchecked")
    void executeBatch_Success() throws Exception {
        // Arrange
        String batchName = "remind-batch";

        CDI<Object> cdiMock = mock(CDI.class);
        mockedCdi.when(CDI::current).thenReturn(cdiMock);

        Instance<BatchExecutor> instanceMock = mock(Instance.class);
        when(cdiMock.select(eq(BatchExecutor.class), any(NamedLiteral.class))).thenReturn(instanceMock);

        BatchExecutor executorMock = mock(BatchExecutor.class);
        when(instanceMock.get()).thenReturn(executorMock);

        // Capture state of history when saved
        java.util.List<BatchStatus> savedStatuses = new java.util.ArrayList<>();
        doAnswer(invocation -> {
            BatchExecutionHistory history = invocation.getArgument(0);
            savedStatuses.add(history.getStatus());
            return null;
        }).when(historyRepository).save(any(BatchExecutionHistory.class));

        // Act
        target.executeBatch(batchName);

        // Assert
        verify(executorMock).execute();
        verify(historyRepository, times(2)).save(any(BatchExecutionHistory.class));

        assertThat(savedStatuses).containsExactly(BatchStatus.RUNNING, BatchStatus.SUCCESS);
    }

    @Test
    @DisplayName("バッチ実行中に例外が発生した際に、履歴がFAILEDで保存され、例外が再スローされること")
    @SuppressWarnings("unchecked")
    void executeBatch_Failure_ShouldSaveFailedHistoryAndRethrow() throws Exception {
        // Arrange
        String batchName = "reflection-batch";
        RuntimeException batchException = new RuntimeException("Something went wrong");

        CDI<Object> cdiMock = mock(CDI.class);
        mockedCdi.when(CDI::current).thenReturn(cdiMock);

        Instance<BatchExecutor> instanceMock = mock(Instance.class);
        when(cdiMock.select(eq(BatchExecutor.class), any(NamedLiteral.class))).thenReturn(instanceMock);

        BatchExecutor executorMock = mock(BatchExecutor.class);
        doThrow(batchException).when(executorMock).execute();
        when(instanceMock.get()).thenReturn(executorMock);

        // Capture state of history when saved
        List<BatchStatus> savedStatuses = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        doAnswer(invocation -> {
            BatchExecutionHistory history = invocation.getArgument(0);
            savedStatuses.add(history.getStatus());
            errorMessages.add(history.getErrorMessage());
            return null;
        }).when(historyRepository).save(any(BatchExecutionHistory.class));

        // Act & Assert
        assertThatThrownBy(() -> target.executeBatch(batchName)).isEqualTo(batchException);

        verify(historyRepository, times(2)).save(any(BatchExecutionHistory.class));

        assertThat(savedStatuses).containsExactly(BatchStatus.RUNNING, BatchStatus.FAILED);
        assertThat(errorMessages.get(1)).isEqualTo("Something went wrong");
    }
}
