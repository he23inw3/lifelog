package jp.he23inw3.asset.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 稼働日または休日・休暇の状態を表す列挙型。
 * Gemini プロンプトの解析コンテキスト等で使用されます。
 */
@Getter
@RequiredArgsConstructor
public enum DayStatus {

    /** 平日 */
    WEEKDAY("平日"),

    /** 祝日/休暇 */
    HOLIDAY("祝日/休暇");

    private final String value;
}
