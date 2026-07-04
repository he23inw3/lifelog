package jp.he23inw3.asset.domain.exception;

/**
 * 操作の実行権限がない（認可エラー）場合にスローされる例外。 HTTP 403 Forbidden にマッピングされる。
 */
public class ForbiddenException extends LifeLogException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
