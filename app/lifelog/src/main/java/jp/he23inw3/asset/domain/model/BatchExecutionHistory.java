package jp.he23inw3.asset.domain.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * バッチの実行履歴を表すドメインモデル。
 */
@Data
@Builder(toBuilder = true)
public class BatchExecutionHistory {

    /** 実行履歴ID (UUIDなど) */
    private String id;

    /** 実行されたバッチの名前 (プロファイル名) */
    private String batchName;

    /** 開始日時 */
    private LocalDateTime startedAt;

    /** 終了日時 */
    private LocalDateTime finishedAt;

    /** 実行結果ステータス */
    private BatchStatus status;

    /** エラーメッセージ（失敗時のみ） */
    private String errorMessage;

    /** エラーのスタックトレース（失敗時のみ） */
    private String errorStackTrace;

    /** セッショントレース識別子 */
    private String traceId;
}
