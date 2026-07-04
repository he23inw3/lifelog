package jp.he23inw3.asset.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class HealthStatusTest {

    @Nested
    @DisplayName("fromValueメソッドのテスト")
    class FromValue {

        @Test
        @DisplayName("HealthStatus.fromValue のパーステスト")
        void testHealthStatusFromValue() {
            assertThat(HealthStatus.fromValue("UP")).isEqualTo(HealthStatus.UP);
            assertThat(HealthStatus.fromValue("  up  ")).isEqualTo(HealthStatus.UP);
            assertThat(HealthStatus.fromValue("DOWN")).isEqualTo(HealthStatus.DOWN);

            // フォールバックケース
            assertThat(HealthStatus.fromValue(null)).isEqualTo(HealthStatus.DOWN);
            assertThat(HealthStatus.fromValue("Unknown")).isEqualTo(HealthStatus.DOWN);
        }
    }
}
