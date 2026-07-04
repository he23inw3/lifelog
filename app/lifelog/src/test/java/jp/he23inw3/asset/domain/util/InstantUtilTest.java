package jp.he23inw3.asset.domain.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InstantUtilTest {

    @Nested
    @DisplayName("nowおよびnowEpochSecondメソッドのテスト")
    class Now {

        @Test
        @DisplayName("nowが現在時刻に近いInstantを返すこと")
        void testNow() {
            Instant result = InstantUtil.now();
            assertThat(result).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("nowEpochSecondが現在時刻に近いエポック秒を返すこと")
        void testNowEpochSecond() {
            long result = InstantUtil.nowEpochSecond();
            long current = Instant.now().getEpochSecond();
            assertThat(result).isCloseTo(current, within(5L));
        }
    }

    @Nested
    @DisplayName("toEpochSecondメソッドのテスト")
    class ToEpochSecond {

        @Test
        @DisplayName("値がnullでない場合に正しいエポック秒に変換されること")
        void testValidInstant() {
            Instant target = Instant.ofEpochSecond(1234567890L);
            assertThat(InstantUtil.toEpochSecond(target)).isEqualTo(1234567890L);
        }

        @Test
        @DisplayName("値がnullの場合にnullを返すこと")
        void testNullInstant() {
            assertThat(InstantUtil.toEpochSecond(null)).isNull();
        }
    }

    @Nested
    @DisplayName("toEpochSecondOrNowメソッドのテスト")
    class ToEpochSecondOrNow {

        @Test
        @DisplayName("値がnullでない場合に正しいエポック秒に変換されること")
        void testValidInstant() {
            Instant target = Instant.ofEpochSecond(1234567890L);
            assertThat(InstantUtil.toEpochSecondOrNow(target)).isEqualTo(1234567890L);
        }

        @Test
        @DisplayName("値がnullの場合に現在時刻に近いエポック秒を返すこと")
        void testNullInstant() {
            Long result = InstantUtil.toEpochSecondOrNow(null);
            long current = Instant.now().getEpochSecond();
            assertThat(result).isCloseTo(current, within(5L));
        }
    }

    @Nested
    @DisplayName("toInstantメソッドのテスト")
    class ToInstant {

        @Test
        @DisplayName("値がnullでない場合に正しいInstantオブジェクトに変換されること")
        void testValidEpochSecond() {
            Long epochSecond = 1234567890L;
            assertThat(InstantUtil.toInstant(epochSecond)).isEqualTo(Instant.ofEpochSecond(1234567890L));
        }

        @Test
        @DisplayName("値がnullの場合にnullを返すこと")
        void testNullEpochSecond() {
            assertThat(InstantUtil.toInstant(null)).isNull();
        }
    }

    @Nested
    @DisplayName("toInstantOrNowメソッドのテスト")
    class ToInstantOrNow {

        @Test
        @DisplayName("値がnullでない場合に正しいInstantオブジェクトに変換されること")
        void testValidEpochSecond() {
            Long epochSecond = 1234567890L;
            assertThat(InstantUtil.toInstantOrNow(epochSecond)).isEqualTo(Instant.ofEpochSecond(1234567890L));
        }

        @Test
        @DisplayName("値がnullの場合に現在時刻に近いInstantを返すこと")
        void testNullEpochSecond() {
            Instant result = InstantUtil.toInstantOrNow(null);
            assertThat(result).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("ofEpochMicroメソッドのテスト")
    class OfEpochMicro {

        @Test
        @DisplayName("値がnullでない場合にマイクロ秒から正しいミリ秒精度のInstantに変換されること")
        void testValidEpochMicros() {
            Long epochMicros = 1234567890123456L;
            assertThat(InstantUtil.ofEpochMicro(epochMicros)).isEqualTo(Instant.ofEpochMilli(1234567890123L));
        }

        @Test
        @DisplayName("値がnullの場合にnullを返すこと")
        void testNullEpochMicros() {
            assertThat(InstantUtil.ofEpochMicro(null)).isNull();
        }
    }
}
