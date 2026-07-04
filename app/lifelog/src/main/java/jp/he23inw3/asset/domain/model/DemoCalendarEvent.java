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
    String calendarId;

    /** 日付 (yyyy-MM-dd) */
    String date;

    /** タイトル */
    String title;

    /** 説明 */
    String description;

    /** 祝日フラグ */
    boolean holiday;

    /** DEMOカレンダーに同期した日時 */
    LocalDateTime syncedAt;
}
