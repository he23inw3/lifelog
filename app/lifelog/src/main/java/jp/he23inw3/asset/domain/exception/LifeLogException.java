package jp.he23inw3.asset.domain.exception;

/**
 * 本アプリケーション (LifeLog) におけるドメイン例外の基底クラス。
 * <p>
 * アプリケーション内で発生する固有のビジネスロジックエラーや実行時エラーは すべてこの例外クラスを継承します。
 */
public class LifeLogException extends RuntimeException {

    /**
     * 指定された詳細メッセージを持つ LifeLogException を構築します。
     *
     * @param message
     *            詳細メッセージ
     */
    public LifeLogException(String message) {
        super(message);
    }

    /**
     * 指定された詳細メッセージおよび原因を持つ LifeLogException を構築します。
     *
     * @param message
     *            詳細メッセージ
     * @param cause
     *            原因となった例外
     */
    public LifeLogException(String message, Throwable cause) {
        super(message, cause);
    }
}
