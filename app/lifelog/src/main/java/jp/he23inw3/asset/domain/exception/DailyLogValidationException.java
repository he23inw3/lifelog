package jp.he23inw3.asset.domain.exception;

/**
 * 日報の入力バリデーション（作業日・稼働時間・作業内容の不足など）でエラーが発生した際にスローされる例外。
 */
public class DailyLogValidationException extends LifeLogException {

    public DailyLogValidationException(String message) {
        super(message);
    }

    public DailyLogValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
