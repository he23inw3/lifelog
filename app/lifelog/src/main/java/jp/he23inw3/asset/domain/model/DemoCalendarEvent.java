package jp.he23inw3.asset.domain.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

/**
 * デモ用のカレンダーイベントを表すドメインモデル。
 */
@Value
@Builder(toBuilder = true)
public class DemoCalendarEvent {

    /** DEMOカレンダーID */
    private String calendarId;

    /** 日付 (yyyy-MM-dd) */
    private String date;

    /** タイトル */
    private String title;

    /** 説明 */
    private String description;

    /** 祝日フラグ */
    private boolean holiday;

    /** DEMOカレンダーに同期した日時 */
    private LocalDateTime syncedAt;
}
