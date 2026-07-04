package jp.he23inw3.asset.domain.exception;

/**
 * デモモードが無効な場合にスローされる例外。 HTTP 403 Forbidden にマッピングされる。
 */
public class DemoModeDisabledException extends LifeLogException {

    public DemoModeDisabledException(String message) {
        super(message);
    }

    public DemoModeDisabledException(String message, Throwable cause) {
        super(message, cause);
    }
}
