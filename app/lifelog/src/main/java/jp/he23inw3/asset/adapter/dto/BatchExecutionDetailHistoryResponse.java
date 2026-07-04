package jp.he23inw3.asset.adapter.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * バッチ実行詳細履歴レスポンス DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchExecutionDetailHistoryResponse {

    /** 実行履歴ID */
    private String id;

    /** バッチ名 */
    private String batchName;

    /** 開始日時 */
    private LocalDateTime startedAt;

    /** 終了日時 */
    private LocalDateTime finishedAt;

    /** 実行ステータス */
    private String status;

    /** エラーメッセージ */
    private String errorMessage;

    /** スタックトレース */
    private String errorStackTrace;

    /** トレースID */
    private String traceId;
}
