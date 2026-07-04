package jp.he23inw3.asset.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * システムおよび外部連携サービスのヘルスステータスを表す列挙型。
 */
@Getter
@RequiredArgsConstructor
public enum HealthStatus {

    /** 稼働中 (正常) */
    UP("UP"),

    /** 停止中 (異常) */
    DOWN("DOWN");

    /**
     * 文字列値に対応する HealthStatus を返します。
     *
     * @param value パース対象の文字列
     * @return 対応する HealthStatus。見つからない場合は DOWN
     */
    public static HealthStatus fromValue(String value) {
        if (StringUtils.isBlank(value)) {
            return DOWN;
        }

        for (HealthStatus s : values()) {
            if (s.value.equalsIgnoreCase(value.trim())) {
                return s;
            }
        }
        return DOWN;
    }

    private final String value;
}
