package jp.he23inw3.asset.adapter.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * バッチ実行履歴取得 API のレスポンス DTO。
 */
@Data
@Builder
public class BatchExecutionHistoryListResponse {

    /** 総件数 */
    private int totalSize;

    /** 取得した実行履歴リスト */
    private List<BatchExecutionHistory> batchExecutionHistories;

    @Data
    @Builder(toBuilder = true)
    public static class BatchExecutionHistory {
        /** 実行履歴ID */
        private String id;

        /** 実行されたバッチの名前 */
        private String batchName;

        /** 開始日時 */
        private LocalDateTime startedAt;

        /** 終了日時 */
        private LocalDateTime finishedAt;

        /** 実行結果ステータス */
        private String status;
    }
}
