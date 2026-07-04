package jp.he23inw3.asset.domain.exception;

/**
 * 外部ゲートウェイサービス (Slack, Google Calendar, Gemini 等) との通信処理中に
 * タイムアウトや通信エラー等が発生した場合にスローされる例外クラス。
 */
public class GatewayException extends LifeLogException {

    /**
     * 指定された詳細メッセージを持つ GatewayException を構築します。
     *
     * @param message
     *            詳細メッセージ
     */
    public GatewayException(String message) {
        super(message);
    }

    /**
     * 指定された詳細メッセージおよび原因を持つ GatewayException を構築します。
     *
     * @param message
     *            詳細メッセージ
     * @param cause
     *            原因となった例外
     */
    public GatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
