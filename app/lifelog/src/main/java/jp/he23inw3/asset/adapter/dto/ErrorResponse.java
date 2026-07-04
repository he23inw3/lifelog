package jp.he23inw3.asset.adapter.dto;

import lombok.Builder;
import lombok.Data;

/**
 * API エラーレスポンス DTO。 ExceptionMapper から返されるエラー情報を統一フォーマットで表現する。
 */
@Data
@Builder
public class ErrorResponse {

    /** HTTP ステータスコード */
    private int status;

    /** エラーの種別 */
    private String errorCode;

    /** ユーザー向けエラーメッセージ */
    private String message;

    /** 詳細情報（開発環境・デバッグ用。本番では省略可） */
    private String detail;

    /** ログ出力されている traceId */
    private String traceId;
}
