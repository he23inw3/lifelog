package jp.he23inw3.asset.adapter.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * 日報ログ一覧取得 API レスポンス DTO。
 */
@Data
@Builder
public class LogListResponse {

    /** 総件数 */
    private int totalSize;

    /** 日報ログのリスト */
    private List<Log> logs;

    @Data
    @Builder(toBuilder = true)
    public static class Log {
        /** ログ日付 */
        private LocalDate logDate;

        /** 休日フラグ */
        private boolean holiday;

        /** 稼働時間 */
        private Double workHours;

        /** 残業時間 */
        private Double overtimeHours;

        /** タスク一覧 */
        private String tasks;

        /** 日記 */
        private String diary;

        /** 感情 */
        private String sentiment;
    }
}
