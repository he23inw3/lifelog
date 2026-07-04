package jp.he23inw3.asset.domain.exception;

/**
 * 外部サービス (Slack, Google Calendar, Firestore, BigQuery, Gemini 等) との連携処理中に
 * 発生した実行時例外を表すクラス。
 */
public class ExternalServiceException extends LifeLogException {

    /**
     * 指定された詳細メッセージを持つ ExternalServiceException を構築します。
     *
     * @param message
     *            詳細メッセージ
     */
    public ExternalServiceException(String message) {
        super(message);
    }

    /**
     * 指定された詳細メッセージおよび原因を持つ ExternalServiceException を構築します。
     *
     * @param message
     *            詳細メッセージ
     * @param cause
     *            原因となった例外
     */
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
