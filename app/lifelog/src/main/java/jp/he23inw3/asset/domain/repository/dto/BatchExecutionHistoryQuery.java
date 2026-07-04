package jp.he23inw3.asset.domain.repository.dto;

import java.time.LocalDateTime;
import jp.he23inw3.asset.domain.model.BatchStatus;
import lombok.Builder;
import lombok.Value;

/**
 * バッチ実行履歴の検索条件を保持する DTO クラス。
 */
@Value
@Builder
public class BatchExecutionHistoryQuery {
    /** 取得件数上限 */
    Integer limit;
    /** オフセット（スキップ件数） */
    Integer offset;
    /** 検索開始日時 */
    LocalDateTime start;
    /** 検索終了日時 */
    LocalDateTime end;
    /** バッチID (バッチ名) */
    String batchId;
    /** 実行ステータス */
    BatchStatus status;
}
