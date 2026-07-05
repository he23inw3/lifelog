package jp.he23inw3.asset.domain.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DateTimeUtilTest {

    private static final ZoneId TOKYO_ZONE = ZoneId.of("Asia/Tokyo");

    @Nested
    @DisplayName("現在日時取得メソッドのテスト")
    class NowDateTime {

        @Test
        @DisplayName("nowLocalDateTimeが東京タイムゾーンの現在日時に近いLocalDateTimeを返すこと")
        void testNowLocalDateTime() {
            LocalDateTime result = DateTimeUtil.nowLocalDateTime();
            LocalDateTime current = LocalDateTime.now(TOKYO_ZONE);
            assertThat(result).isCloseTo(current, within(5, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("nowLocalDateが東京タイムゾーンの現在日付と一致すること")
        void testNowLocalDate() {
            LocalDate result = DateTimeUtil.nowLocalDate();
            LocalDate current = LocalDate.now(TOKYO_ZONE);
            assertThat(result).isEqualTo(current);
        }

        @Test
        @DisplayName("nowLocalTimeが東京タイムゾーンの現在時刻に近いLocalTimeを返すこと")
        void testNowLocalTime() {
            LocalTime result = DateTimeUtil.nowLocalTime();
            LocalTime current = LocalTime.now(TOKYO_ZONE);
            assertThat(result).isCloseTo(current, within(5, ChronoUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("変換メソッドのテスト")
    class ConvertMethods {

        @Test
        @DisplayName("toLocalDateTimeがInstantを東京タイムゾーンのLocalDateTimeに正しく変換すること")
        void testToLocalDateTime() {
            Instant instant = Instant.parse("2026-07-05T13:36:11.123456789Z");
            LocalDateTime expected = LocalDateTime.of(2026, 7, 5, 22, 36, 11, 123456789);
            assertThat(DateTimeUtil.toLocalDateTime(instant)).isEqualTo(expected);
            assertThat(DateTimeUtil.toLocalDateTime(null)).isNull();
        }

        @Test
        @DisplayName("toBigQueryTimestampStringがInstantをBigQuery用タイムスタンプ文字列に正しく変換すること")
        void testToBigQueryTimestampString() {
            // ナノ秒精度（9桁）の Instant を指定
            Instant instant = Instant.parse("2026-07-05T13:36:11.123456789Z");
            // マイクロ秒精度（6桁）かつ UTC (Z付き) で出力されることを検証
            assertThat(DateTimeUtil.toBigQueryTimestampString(instant)).isEqualTo("2026-07-05T13:36:11.123456Z");
            assertThat(DateTimeUtil.toBigQueryTimestampString(null)).isNull();
        }
    }
}
