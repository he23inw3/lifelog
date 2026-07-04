package jp.he23inw3.asset.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BatchStatusTest {

    @Nested
    @DisplayName("fromValueメソッドのテスト")
    class FromValue {

        @Test
        @DisplayName("BatchStatus.fromValue のパーステスト")
        void testBatchStatusFromValue() {
            assertThat(BatchStatus.fromValue("RUNNING")).isEqualTo(BatchStatus.RUNNING);
            assertThat(BatchStatus.fromValue("  running  ")).isEqualTo(BatchStatus.RUNNING);
            assertThat(BatchStatus.fromValue("SUCCESS")).isEqualTo(BatchStatus.SUCCESS);
            assertThat(BatchStatus.fromValue("FAILED")).isEqualTo(BatchStatus.FAILED);

            // フォールバックケース
            assertThat(BatchStatus.fromValue(null)).isEqualTo(BatchStatus.FAILED);
            assertThat(BatchStatus.fromValue("Unknown")).isEqualTo(BatchStatus.FAILED);
        }
    }
}
