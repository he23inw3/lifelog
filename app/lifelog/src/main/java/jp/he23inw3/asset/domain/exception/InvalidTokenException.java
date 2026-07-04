package jp.he23inw3.asset.domain.exception;

/**
 * 連携用の一時トークンが無効または期限切れである場合にスローされる例外。 HTTP 400 Bad Request にマッピングされる。
 */
public class InvalidTokenException extends LifeLogException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
