package jp.he23inw3.asset.adapter.constant;

/**
 * API エラーの種別を表現する定数クラス。
 */
public final class ErrorCode {
    /** 400 Bad Request: リクエスト内容が不正な場合 */
    public static final String BAD_REQUEST = "BAD_REQUEST";

    /** 400 Bad Request: バリデーションエラーの場合 */
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";

    /** 403 Forbidden: 認可エラーやデモモード制限など */
    public static final String FORBIDDEN = "FORBIDDEN";

    /** 404 Not Found: リソースが存在しない場合 */
    public static final String NOT_FOUND = "NOT_FOUND";

    /** 409 Conflict: リソースが重複している場合 */
    public static final String CONFLICT = "CONFLICT";

    /** 500 Internal Server Error: システムの予期しない内部エラー */
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

    /** 502 Bad Gateway: 外部サービス実行時エラー */
    public static final String EXTERNAL_SERVICE_ERROR = "EXTERNAL_SERVICE_ERROR";

    /** 502 Bad Gateway: 一般的なゲートウェイエラー */
    public static final String GATEWAY_ERROR = "GATEWAY_ERROR";

    /** 504 Gateway Timeout: 外部サービス接続タイムアウト */
    public static final String GATEWAY_TIMEOUT = "GATEWAY_TIMEOUT";

    private ErrorCode() {
        // インスタンス化禁止
    }
}
