package jp.he23inw3.asset.infrastructure.common;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.commons.lang3.ArrayUtils;

/**
 * messages.properties からログメッセージやエラーメッセージを取得するヘルパークラス。
 */
public class MessageHelper {

    private static final ResourceBundle RESOURCE_BUNDLE;

    static {
        ResourceBundle tempBundle = null;
        try {
            tempBundle = ResourceBundle.getBundle("messages", Locale.getDefault(), MessageHelper.class.getClassLoader());
        } catch (MissingResourceException e) {
            // フォールバック
        }
        RESOURCE_BUNDLE = tempBundle;
    }

    /**
     * 指定されたキーのメッセージを取得し、引数を埋め込みます。
     *
     * @param key メッセージキー
     * @param args 埋め込む引数
     * @return フォーマットされたメッセージ
     */
    public static String getMessage(String key, Object... args) {
        if (RESOURCE_BUNDLE == null) {
            return "Missing bundle: " + key;
        }
        try {
            String pattern = RESOURCE_BUNDLE.getString(key);

            if (ArrayUtils.isEmpty(args)) {
                return pattern;
            }
            return MessageFormat.format(pattern, args);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    private MessageHelper() {
        // インスタンス化禁止
    }
}
