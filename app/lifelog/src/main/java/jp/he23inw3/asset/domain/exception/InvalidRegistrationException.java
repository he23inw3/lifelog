package jp.he23inw3.asset.domain.exception;

/**
 * ユーザー初回登録時の入力値（Slack ユーザー ID や連携トークンなど）が不正な場合にスローされる例外。 HTTP 400 Bad Request にマッピングされる。
 */
public class InvalidRegistrationException extends LifeLogException {

    public InvalidRegistrationException(String message) {
        super(message);
    }

    public InvalidRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
