package jp.he23inw3.asset.adapter.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import jp.he23inw3.asset.domain.exception.InvalidRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DateParserTest {

    @Nested
    @DisplayName("日付文字列パース")
    class ParseDate {

        @Test
        @DisplayName("正しいフォーマット(YYYY-MM-DD)の場合、LocalDateが返されること")
        void parseDate_Success() {
            LocalDate result = DateParser.parseDate("2026-06-30");
            assertThat(result).isEqualTo(LocalDate.of(2026, 6, 30));
        }

        @Test
        @DisplayName("不正なフォーマットの場合、InvalidRequestExceptionがスローされること")
        void parseDate_InvalidFormat_ThrowsInvalidRequestException() {
            assertThatThrownBy(() -> DateParser.parseDate("2026/06/30"))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessage("日付フォーマットが正しくありません。YYYY-MM-DD形式で指定してください。");

            assertThatThrownBy(() -> DateParser.parseDate("invalid"))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessage("日付フォーマットが正しくありません。YYYY-MM-DD形式で指定してください。");

            assertThatThrownBy(() -> DateParser.parseDate(""))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessage("日付フォーマットが正しくありません。YYYY-MM-DD形式で指定してください。");

            assertThatThrownBy(() -> DateParser.parseDate(null))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessage("日付フォーマットが正しくありません。YYYY-MM-DD形式で指定してください。");
        }
    }
}
