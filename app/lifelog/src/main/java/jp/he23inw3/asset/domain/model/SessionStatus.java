package jp.he23inw3.asset.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * ユーザーとの対話フロー（ヒアリングセッション）の進行状況を示すステータス列挙型。
 */
@Getter
@RequiredArgsConstructor
public enum SessionStatus {

    /** 勤務時間（work_hours）の入力待ち状態 */
    WAITING_WORK_HOURS("WAITING_WORK_HOURS"),

    /** ユーザーの確定ボタン押し下げ待ち状態 */
    AWAITING_CONFIRMATION("AWAITING_CONFIRMATION");

    /**
     * 文字列値に対応する SessionStatus を返します。
     *
     * @param value パース対象の文字列
     * @return 対応する SessionStatus。見つからない場合は null
     */
    public static SessionStatus fromValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        for (SessionStatus s : values()) {
            if (s.value.equalsIgnoreCase(value.trim())) {
                return s;
            }
        }
        return null;
    }

    private final String value;
}
