package jp.he23inw3.asset.domain.repository;

import java.time.LocalDate;
import java.util.List;
import jp.he23inw3.asset.domain.model.DemoCalendarEvent;

/**
 * デモ用のカレンダーイベントを管理するリポジトリインターフェース。
 */
public interface DemoCalendarRepository {

    /**
     * 指定されたカレンダーIDと期間に該当するデモ用カレンダーイベント一覧を取得します。
     *
     * @param calendarId カレンダーID
     * @param start 取得開始日
     * @param end 取得終了日
     * @return デモ用カレンダーイベントのリスト
     */
    List<DemoCalendarEvent> findByCalendarIdAndPeriod(String calendarId, LocalDate start, LocalDate end);
}
