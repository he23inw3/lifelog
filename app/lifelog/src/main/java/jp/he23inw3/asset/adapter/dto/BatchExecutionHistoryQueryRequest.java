package jp.he23inw3.asset.adapter.dto;

import jakarta.ws.rs.QueryParam;
import lombok.Data;

/**
 * バッチ実行履歴取得 API のクエリパラメータを保持するリクエストオブジェクト。
 */
@Data
public class BatchExecutionHistoryQueryRequest {

    /** 取得件数上限 */
    @QueryParam("limit")
    private Integer limit;

    /** オフセット（スキップ件数） */
    @QueryParam("offset")
    private Integer offset;

    /** 検索開始日 (YYYY-MM-DD) */
    @QueryParam("start")
    private String start;

    /** 検索終了日 (YYYY-MM-DD) */
    @QueryParam("end")
    private String end;

    /** バッチID (バッチ名) */
    @QueryParam("batchId")
    private String batchId;

    /** 実行結果ステータス */
    @QueryParam("status")
    private String status;
}
