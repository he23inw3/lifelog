package jp.he23inw3.asset.adapter.dto;

import jakarta.ws.rs.QueryParam;
import lombok.Data;

/**
 * 日報検索 API のクエリパラメータを保持するリクエストオブジェクト。
 */
@Data
public class LogSearchQueryRequest {

    /** Slack ユーザーID */
    @QueryParam("user")
    private String user;

    /** 検索開始日 (YYYY-MM-DD) */
    @QueryParam("from")
    private String from;

    /** 検索終了日 (YYYY-MM-DD) */
    @QueryParam("to")
    private String to;

    /** 休暇フラグ */
    @QueryParam("holiday")
    private Boolean holiday;

    /** 感情 */
    @QueryParam("sentiment")
    private String sentiment;
}
