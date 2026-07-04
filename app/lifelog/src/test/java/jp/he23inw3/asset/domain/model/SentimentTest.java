package jp.he23inw3.asset.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SentimentTest {

    @Nested
    @DisplayName("fromValueメソッドのテスト")
    class FromValue {

        @Test
        @DisplayName("Sentiment.fromValue のパーステスト")
        void testSentimentFromValue() {
            assertThat(Sentiment.fromValue("Happy")).isEqualTo(Sentiment.HAPPY);
            assertThat(Sentiment.fromValue("  happy  ")).isEqualTo(Sentiment.HAPPY);
            assertThat(Sentiment.fromValue("Tired")).isEqualTo(Sentiment.TIRED);
            assertThat(Sentiment.fromValue("Neutral")).isEqualTo(Sentiment.NEUTRAL);
            assertThat(Sentiment.fromValue("Stressed")).isEqualTo(Sentiment.STRESSED);
            assertThat(Sentiment.fromValue("Relaxed")).isEqualTo(Sentiment.RELAXED);
            assertThat(Sentiment.fromValue("Bad")).isEqualTo(Sentiment.BAD);

            // フォールバックケース
            assertThat(Sentiment.fromValue(null)).isEqualTo(Sentiment.NEUTRAL);
            assertThat(Sentiment.fromValue("Unknown")).isEqualTo(Sentiment.NEUTRAL);
        }
    }

    @Nested
    @DisplayName("getNameOrDefaultメソッドのテスト")
    class GetNameOrDefault {

        @Test
        @DisplayName("getNameOrDefault のテスト")
        void testGetNameOrDefault() {
            assertThat(Sentiment.getNameOrDefault(Sentiment.HAPPY)).isEqualTo("HAPPY");
            assertThat(Sentiment.getNameOrDefault(null)).isEqualTo("NEUTRAL");
        }
    }

    @Nested
    @DisplayName("getNameOrNullメソッドのテスト")
    class GetNameOrNull {

        @Test
        @DisplayName("getNameOrNull のテスト")
        void testGetNameOrNull() {
            assertThat(Sentiment.getNameOrNull(Sentiment.HAPPY)).isEqualTo("HAPPY");
            assertThat(Sentiment.getNameOrNull(null)).isNull();
        }
    }

    @Nested
    @DisplayName("getValueOrDefaultメソッドのテスト")
    class GetValueOrDefault {

        @Test
        @DisplayName("getValueOrDefault のテスト")
        void testGetValueOrDefault() {
            assertThat(Sentiment.getValueOrDefault(Sentiment.HAPPY)).isEqualTo("Happy");
            assertThat(Sentiment.getValueOrDefault(null)).isEqualTo("Neutral");
        }
    }
}
