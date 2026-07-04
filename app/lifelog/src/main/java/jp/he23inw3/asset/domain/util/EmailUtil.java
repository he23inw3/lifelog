package jp.he23inw3.asset.domain.util;

import org.apache.commons.lang3.StringUtils;

/**
 * メールアドレスに関するユーティリティ処理を提供するクラス。
 */
public final class EmailUtil {

    /**
     * メールアドレスからユーザー名（@の前の部分）を安全に抽出します。
     *
     * @param email メールアドレス
     * @return 抽出されたユーザー名。emailがnullまたは空の場合は空文字列、@が含まれない場合はメールアドレス全体を返します。
     */
    public static String extractUserName(String email) {
        if (StringUtils.isEmpty(email)) {
            return "";
        }
        int atIndex = email.indexOf('@');
        if (atIndex == -1) {
            return email;
        }
        return email.substring(0, atIndex);
    }

    private EmailUtil() {
        // インスタンス化禁止
    }
}
