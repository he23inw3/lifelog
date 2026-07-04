package jp.he23inw3.asset.domain.exception;

/**
 * 要求されたリソースが既に存在する場合にスローされる例外。 HTTP 409 Conflict にマッピングされる。
 */
public class AlreadyExistsException extends LifeLogException {

    public AlreadyExistsException(String message) {
        super(message);
    }

    public AlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
