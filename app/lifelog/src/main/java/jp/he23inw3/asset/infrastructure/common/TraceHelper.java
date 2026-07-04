package jp.he23inw3.asset.infrastructure.common;

import io.opentelemetry.api.trace.Span;

/**
 * OpenTelemetry の現在のトレース ID を取得するユーティリティクラス。
 * <p>
 * ログへの明示的な traceId 付与や BigQuery など外部ストレージへの記録に使用する。 
 * Quarkus の {@code quarkus-opentelemetry} 拡張が MDC へ {@code traceId} を自動注入するため、 ログフォーマットに {@code %X{traceId}}
 * を指定することで大部分は自動付与される。 本クラスは明示的に traceId 文字列が必要な箇所（BigQuery カラムへの保存等）で利用する。
 */
public final class TraceHelper {

    /** 有効なスパンが存在しない場合のフォールバック値 */
    public static final String EMPTY_TRACE_ID = "00000000000000000000000000000000";

    /**
     * 現在のスパンから traceId（32桁 16進数文字列）を返す。
     * <p>
     * 有効なスパンが存在しない場合（バッチ処理・テスト等）は {@link #EMPTY_TRACE_ID} を返す。
     *
     * @return 32桁の traceId 文字列
     */
    public static String currentTraceId() {
        String traceId = Span.current().getSpanContext().getTraceId();
        return traceId != null ? traceId : EMPTY_TRACE_ID;
    }

    private TraceHelper() {
        // インスタンス化禁止
    }
}
