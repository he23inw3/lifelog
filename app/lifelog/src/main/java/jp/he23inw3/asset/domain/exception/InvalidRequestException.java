package jp.he23inw3.asset.domain.exception;

/**
 * リクエストパラメータやクエリ条件が無効な場合にスローされる例外。 HTTP 400 Bad Request にマッピングされる。
 */
public class InvalidRequestException extends LifeLogException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
