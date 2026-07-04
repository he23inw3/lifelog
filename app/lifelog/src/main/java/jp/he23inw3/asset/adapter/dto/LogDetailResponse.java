package jp.he23inw3.asset.adapter.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 日報ログ詳細・登録・解析 API レスポンス DTO。
 */
@Data
@Builder
public class LogDetailResponse {

    /** Slack ユーザー ID */
    private String slackUserId;

    /** ログ日付 */
    private LocalDate logDate;

    /** 未加工テキスト */
    private String rawText;

    /** 休日フラグ */
    private boolean holiday;

    /** タスク一覧 */
    private String tasks;

    /** 稼働時間 */
    private Double workHours;

    /** 残業時間 */
    private Double overtimeHours;

    /** 日記 */
    private String diary;

    /** 感情 */
    private String sentiment;

    /** 作成日時 */
    private LocalDateTime createdAt;

    /** 更新日時 */
    private LocalDateTime updatedAt;
}
