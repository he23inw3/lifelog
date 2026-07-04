package jp.he23inw3.asset.domain.exception;

/**
 * 暗号化または復号処理中にエラーが発生した場合にスローされる例外クラス。
 */
public class CryptoException extends LifeLogException {

    /**
     * 指定された詳細メッセージを持つ CryptoException を構築します。
     *
     * @param message
     *            詳細メッセージ
     */
    public CryptoException(String message) {
        super(message);
    }

    /**
     * 指定された詳細メッセージおよび原因を持つ CryptoException を構築します。
     *
     * @param message
     *            詳細メッセージ
     * @param cause
     *            原因となった例外
     */
    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
