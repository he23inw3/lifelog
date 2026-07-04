package jp.he23inw3.asset.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * バッチプロセスの実行結果および処理状態を示すステータス列挙型。
 */
@Getter
@RequiredArgsConstructor
public enum BatchStatus {

    /** 実行中 */
    RUNNING("RUNNING"),

    /** 正常終了 */
    SUCCESS("SUCCESS"),

    /** 異常終了（失敗） */
    FAILED("FAILED");

    /**
     * 文字列値に対応する BatchStatus を返します。
     *
     * @param value パース対象の文字列
     * @return 対応する BatchStatus。見つからない場合は FAILED（フォールバック）
     */
    public static BatchStatus fromValue(String value) {
        if (StringUtils.isBlank(value)) {
            return FAILED;
        }

        for (BatchStatus s : values()) {
            if (s.value.equalsIgnoreCase(StringUtils.trim(value))) {
                return s;
            }
        }
        return FAILED;
    }

    private final String value;
}
