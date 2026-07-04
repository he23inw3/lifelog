package jp.he23inw3.asset.domain.exception;

/**
 * デモAPI呼び出し時のパラメータが不正な場合にスローされる例外。 HTTP 400 Bad Request にマッピングされる。
 */
public class InvalidDemoParameterException extends LifeLogException {

    public InvalidDemoParameterException(String message) {
        super(message);
    }

    public InvalidDemoParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
