package jp.he23inw3.asset.domain.util;

import java.time.Instant;

/**
 * Instant に関する変換や現在時刻取得の共通処理を提供するユーティリティクラス。
 */
public final class InstantUtil {

    /**
     * 現在の Instant を取得します。
     *
     * @return 現在の {@link Instant}
     */
    public static Instant now() {
        return Instant.now();
    }

    /**
     * 現在時刻のエポック秒を取得します。
     *
     * @return 現在時刻のエポック秒
     */
    public static long nowEpochSecond() {
        return Instant.now().getEpochSecond();
    }

    /**
     * 指定された Instant のエポック秒を取得します。 instant が null の場合は null を返します。
     *
     * @param instant 変換対象の Instant
     * @return エポック秒、または null
     */
    public static Long toEpochSecond(Instant instant) {
        return instant != null ? instant.getEpochSecond() : null;
    }

    /**
     * 指定された Instant のエポック秒を取得します。 instant が null の場合は現在時刻のエポック秒を返します。
     *
     * @param instant 変換対象の Instant
     * @return エポック秒
     */
    public static Long toEpochSecondOrNow(Instant instant) {
        return instant != null ? instant.getEpochSecond() : Instant.now().getEpochSecond();
    }

    /**
     * エポック秒から Instant オブジェクトを構築します。 epochSecond が null の場合は null を返します。
     *
     * @param epochSecond エポック秒
     * @return Instant オブジェクト、または null
     */
    public static Instant toInstant(Long epochSecond) {
        return epochSecond != null ? Instant.ofEpochSecond(epochSecond) : null;
    }

    /**
     * エポック秒から Instant オブジェクトを構築します。 epochSecond が null の場合は現在時刻 (Instant.now()) を返します。
     *
     * @param epochSecond エポック秒
     * @return Instant オブジェクト
     */
    public static Instant toInstantOrNow(Long epochSecond) {
        return epochSecond != null ? Instant.ofEpochSecond(epochSecond) : Instant.now();
    }

    /**
     * エポックマイクロ秒から Instant オブジェクトを構築します。 epochMicros が null の場合は null を返します。
     *
     * @param epochMicros エポックマイクロ秒
     * @return Instant オブジェクト、または null
     */
    public static Instant ofEpochMicro(Long epochMicros) {
        return epochMicros != null ? Instant.ofEpochMilli(epochMicros / 1000) : null;
    }

    private InstantUtil() {
        // インスタンス化禁止
    }
}
