package jp.he23inw3.asset.infrastructure.demo;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import java.time.LocalDate;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.gateway.GoogleCalendarGateway;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import jp.he23inw3.asset.infrastructure.google.GoogleCalendarGatewayImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * デモ/本番を透過的に切り替える {@link GoogleCalendarGateway} ルータークラス。
 * <p>
 * {@code @Alternative @Priority(1)} により、このクラスが CDI の注入候補として優先されます。
 * {@code lifelog.demo.enabled} が {@code true} の場合は
 * {@link DemoCalendarGatewayImpl}、 それ以外は {@link GoogleCalendarGatewayImpl}
 * に委譲します。
 */
@Slf4j
@Alternative
@Priority(1)
@ApplicationScoped
@RequiredArgsConstructor
public class GoogleCalendarGatewayRouter implements GoogleCalendarGateway {

    private final LifeLogConfig config;

    private final GoogleCalendarGatewayImpl realGateway;

    private final DemoCalendarGatewayImpl demoGateway;

    /**
     * 指定されたカレンダーにおいて、特定の日付が祝日または有給休暇・休暇であるかを判定します。
     *
     * @param calendarId 対象のカレンダーID
     * @param date 判定対象の日付
     * @return 祝日または休暇である場合は true、それ以外は false
     */
    @Override
    public boolean isHolidayOrPaidLeave(String calendarId, LocalDate date) {
        return getTarget().isHolidayOrPaidLeave(calendarId, date);
    }

    /**
     * 指定されたカレンダーにイベント（有給休暇の登録等）を登録または更新（既存イベントがある場合）します。
     *
     * @param calendarId 対象のカレンダーID
     * @param date 登録・更新対象の日付
     * @param title イベントのタイトル
     * @param description イベントの説明
     */
    @Override
    public void insertOrUpdateEvent(String calendarId, LocalDate date, String title, String description) {
        log.debug(MessageHelper.getMessage("infra.calendar.router.debug", config.demo().enabled(), calendarId, date));
        getTarget().insertOrUpdateEvent(calendarId, date, title, description);
    }

    private GoogleCalendarGateway getTarget() {
        return config.demo().enabled() ? demoGateway : realGateway;
    }
}
