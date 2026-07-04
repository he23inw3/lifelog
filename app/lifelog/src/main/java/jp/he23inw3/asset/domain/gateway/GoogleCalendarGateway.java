package jp.he23inw3.asset.domain.gateway;

import java.time.LocalDate;

/**
 * Google Calendar サービスと連携し、休暇判定やイベントの自動登録・更新を行うゲートウェイインターフェース。
 */
public interface GoogleCalendarGateway {

    /**
     * 指定されたカレンダーにおいて、特定の日付が祝日または有給休暇・休暇であるかを判定します。
     *
     * @param calendarId 対象のカレンダーID
     * @param date 判定対象の日付
     * @return 祝日または休暇である場合は true、それ以外は false
     */
    boolean isHolidayOrPaidLeave(String calendarId, LocalDate date);

    /**
     * 指定されたカレンダーにイベント（有給休暇の登録等）を登録または更新（既存イベントがある場合）します。
     *
     * @param calendarId 対象のカレンダーID
     * @param date 登録・更新対象の日付
     * @param title イベントのタイトル
     * @param description イベントの説明
     */
    void insertOrUpdateEvent(String calendarId, LocalDate date, String title, String description);
}
