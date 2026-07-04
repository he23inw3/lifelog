package jp.he23inw3.asset.domain.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * 日時に関する統一的な現在時刻取得やタイムゾーン操作を提供するユーティリティクラス。
 */
public final class DateTimeUtil {

    /** 東京のタイムゾーン ID */
    public static final ZoneId TOKYO_ZONE = ZoneId.of("Asia/Tokyo");

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

    private DateTimeUtil() {
        // インスタンス化禁止
    }
}
