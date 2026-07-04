package jp.he23inw3.asset.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SlackLinkageTokenTest {

    @Nested
    @DisplayName("isExpiredメソッドのテスト")
    class IsExpired {

        @Test
        @DisplayName("expiresAtが未来の時刻である場合は期限切れではない(false)と判定されること")
        void testNotExpired() {
            SlackLinkageToken token = SlackLinkageToken.builder()
                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .build();

            assertThat(token.isExpired()).isFalse();
        }

        @Test
        @DisplayName("expiresAtが過去の時刻である場合は期限切れ(true)と判定されること")
        void testExpired() {
            SlackLinkageToken token = SlackLinkageToken.builder()
                    .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                    .build();

            assertThat(token.isExpired()).isTrue();
        }

        @Test
        @DisplayName("expiresAtがnullである場合は期限切れではない(false)と判定されること")
        void testNullExpiresAt() {
            SlackLinkageToken token = SlackLinkageToken.builder()
                    .expiresAt(null)
                    .build();

            assertThat(token.isExpired()).isFalse();
        }
    }
}
