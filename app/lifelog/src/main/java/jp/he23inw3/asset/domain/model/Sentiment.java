package jp.he23inw3.asset.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * ユーザーの入力テキスト（日記）から Gemini により分析された感情データを表す列挙型。
 */
@Getter
@RequiredArgsConstructor
public enum Sentiment {

    /** 嬉しい、幸せ */
    HAPPY("Happy"),

    /** 疲れている */
    TIRED("Tired"),

    /** 普通、特記事項なし */
    NEUTRAL("Neutral"),

    /** ストレスを感じている */
    STRESSED("Stressed"),

    /** リラックスしている */
    RELAXED("Relaxed"),

    /** 調子が悪い、良くない */
    BAD("Bad");

    /**
     * 文字列値に対応する Sentiment を返します。
     *
     * @param value パース対象の感情文字列
     * @return 対応する Sentiment。見つからない場合は NEUTRAL
     */
    public static Sentiment fromValue(String value) {
        if (StringUtils.isBlank(value)) {
            return NEUTRAL;
        }

        for (Sentiment s : values()) {
            if (s.value.equalsIgnoreCase(value.trim())) {
                return s;
            }
        }
        return NEUTRAL;
    }

    /**
     * 指定された Sentiment の name を返します。 sentiment が null の場合は NEUTRAL.name() を返します。
     *
     * @param sentiment 対象の Sentiment（null 可）
     * @return 感情の名称 (e.g., "NEUTRAL", "HAPPY")
     */
    public static String getNameOrDefault(Sentiment sentiment) {
        return (sentiment != null ? sentiment : NEUTRAL).name();
    }

    /**
     * 指定された Sentiment の name を返します。 sentiment が null の場合は null を返します。
     *
     * @param sentiment 対象の Sentiment（null 可）
     * @return 感情の名称 (e.g., "NEUTRAL", "HAPPY")、または null
     */
    public static String getNameOrNull(Sentiment sentiment) {
        return sentiment != null ? sentiment.name() : null;
    }

    /**
     * 指定された Sentiment の value を返します。 sentiment が null の場合は NEUTRAL.getValue() を返します。
     *
     * @param sentiment 対象の Sentiment（null 可）
     * @return 感情のラベル値 (e.g., "Neutral", "Happy")
     */
    public static String getValueOrDefault(Sentiment sentiment) {
        return (sentiment != null ? sentiment : NEUTRAL).getValue();
    }

    private final String value;
}
