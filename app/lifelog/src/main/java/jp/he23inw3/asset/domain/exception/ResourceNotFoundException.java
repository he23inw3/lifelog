package jp.he23inw3.asset.domain.exception;

/**
 * 要求されたリソース（ユーザー設定・セッション等）が存在しない場合にスローされる例外。 HTTP 404 にマッピングされる。
 */
public class ResourceNotFoundException extends LifeLogException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
