package jp.he23inw3.asset.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import jp.he23inw3.asset.usecase.RemindCheckUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RemindCheckExecutorTest {

    @Mock
    RemindCheckUseCase useCase;

    @InjectMocks
    RemindCheckExecutor target;

    @Test
    @DisplayName("RemindCheckExecutorの処理呼び出しおよびバッチ名取得テスト")
    void testExecutor() throws Exception {
        // Act & Assert for getBatchName
        assertThat(target.getBatchName()).isEqualTo("BE-BATCH001(RemindCheck)");

        // Act for invoke
        target.invoke();

        // Assert for invoke delegation
        verify(useCase).checkAndSendRemind();
    }
}
