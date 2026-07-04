package jp.he23inw3.asset.adapter.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * デモ用カレンダーイベント一覧レスポンス DTO。
 */
@Data
@Builder
public class DemoCalendarListResponse {

    /** イベント総数 */
    private int totalSize;

    /** カレンダーイベント一覧 */
    private List<CalendarEvent> calendarEvents;

    @Data
    @Builder
    public static class CalendarEvent {
        /** カレンダー ID */
        private String calendarId;
        /** イベント日付 (YYYY-MM-DD 形式) */
        private String date;
        /** イベントタイトル */
        private String title;
        /** イベント詳細 */
        private String description;
        /** 休暇フラグ */
        private boolean holiday;
        /** 同期日時 */
        private LocalDateTime syncedAt;
    }
}
