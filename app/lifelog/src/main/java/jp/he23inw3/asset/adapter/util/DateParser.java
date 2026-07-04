package jp.he23inw3.asset.adapter.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import jp.he23inw3.asset.domain.exception.InvalidRequestException;
import org.apache.commons.lang3.StringUtils;

/**
 * リクエストパラメータの日付解析ユーティリティクラス。
 */
public final class DateParser {

    /**
     * 日付文字列を {@link LocalDate} にパースします。 
     * 
     * パース失敗時は {@link InvalidRequestException} をスローします。
     *
     * @param dateStr YYYY-MM-DD 形式の日付文字列
     * @return パース結果の {@link LocalDate}
     * @throws InvalidRequestException 日付フォーマットが不正な場合
     */
    public static LocalDate parseDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            throw new InvalidRequestException("日付フォーマットが正しくありません。YYYY-MM-DD形式で指定してください。");
        }

        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new InvalidRequestException("日付フォーマットが正しくありません。YYYY-MM-DD形式で指定してください。");
        }
    }

    private DateParser() {
        // インスタンス化禁止
    }
}
