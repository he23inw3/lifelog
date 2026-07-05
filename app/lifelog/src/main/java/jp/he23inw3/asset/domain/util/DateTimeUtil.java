package jp.he23inw3.asset.domain.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * 日時に関する統一的な現在時刻取得やタイムゾーン操作を提供するユーティリティクラス。
 */
public final class DateTimeUtil {

    /** 東京のタイムゾーン ID */
    public static final ZoneId TOKYO_ZONE = ZoneId.of("Asia/Tokyo");

    /** BigQuery 向けのタイムスタンプフォーマッタ（マイクロ秒精度、UTC） */
    public static final DateTimeFormatter BQ_TIMESTAMP_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, 6, 6, true)
            .appendPattern("XXX")
            .toFormatter()
            .withZone(ZoneOffset.UTC);

    /**
     * 東京タイムゾーンの現在の LocalDateTime を取得します。
     *
     * @return 現在の {@link LocalDateTime}
     */
    public static LocalDateTime nowLocalDateTime() {
        return LocalDateTime.now(TOKYO_ZONE);
    }

    /**
     * 東京タイムゾーンの現在の LocalDate を取得します。
     *
     * @return 現在の {@link LocalDate}
     */
    public static LocalDate nowLocalDate() {
        return LocalDate.now(TOKYO_ZONE);
    }

    /**
     * 東京タイムゾーンの現在の LocalTime を取得します。
     *
     * @return 現在の {@link LocalTime}
     */
    public static LocalTime nowLocalTime() {
        return LocalTime.now(TOKYO_ZONE);
    }

    /**
     * Instant を東京タイムゾーンの LocalDateTime に変換します。
     * instant が null の場合は null を返します。
     *
     * @param instant 変換対象の Instant
     * @return 変換後の {@link LocalDateTime}、または null
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, TOKYO_ZONE) : null;
    }

    /**
     * Instant を BigQuery 向けのタイムスタンプ文字列（マイクロ秒精度、UTC、Z付き）にフォーマットします。
     * instant が null の場合は null を返します。
     *
     * @param instant 変換対象の Instant
     * @return フォーマットされたタイムスタンプ文字列、または null
     */
    public static String toBigQueryTimestampString(Instant instant) {
        return instant != null ? BQ_TIMESTAMP_FORMATTER.format(instant) : null;
    }

    private DateTimeUtil() {
        // インスタンス化禁止
    }
}
