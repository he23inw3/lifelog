package jp.he23inw3.asset.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SessionStatusTest {

    @Nested
    @DisplayName("fromValueメソッドのテスト")
    class FromValue {

        @Test
        @DisplayName("SessionStatus.fromValue のパーステスト")
        void testSessionStatusFromValue() {
            assertThat(SessionStatus.fromValue("WAITING_WORK_HOURS")).isEqualTo(SessionStatus.WAITING_WORK_HOURS);
            assertThat(SessionStatus.fromValue("  waiting_work_hours  ")).isEqualTo(SessionStatus.WAITING_WORK_HOURS);
            assertThat(SessionStatus.fromValue("AWAITING_CONFIRMATION")).isEqualTo(SessionStatus.AWAITING_CONFIRMATION);

            // フォールバックケース
            assertThat(SessionStatus.fromValue(null)).isNull();
            assertThat(SessionStatus.fromValue("Unknown")).isNull();
        }
    }
}
