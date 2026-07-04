package jp.he23inw3.asset.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.exception.DemoModeDisabledException;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.model.DemoCalendarEvent;
import jp.he23inw3.asset.domain.model.DemoMessage;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.DemoCalendarRepository;
import jp.he23inw3.asset.domain.repository.DemoMessageRepository;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * デモモード用の疑似カレンダーおよび疑似 Slack メッセージ取得処理を制御するユースケース。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DemoUseCase {

    private final LifeLogConfig config;

    private final UserSettingRepository userSettingRepository;

    private final DemoCalendarRepository demoCalendarRepository;

    private final DemoMessageRepository demoMessageRepository;

    /**
     * デモ用のカレンダーイベント一覧を取得します。
     *
     * @param yearMonth
     *            対象年月
     * @return カレンダーイベントのリスト
     */
    public List<DemoCalendarEvent> getDemoCalendar(YearMonth yearMonth) {
        ensureDemoModeEnabled();

        String email = config.demo().userEmail();
        UserSetting setting = userSettingRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("デモユーザー設定が見つかりません。"));

        String calendarId = setting.getGoogleCalendarId();
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<DemoCalendarEvent> events = demoCalendarRepository.findByCalendarIdAndPeriod(calendarId, start, end);

        return events.stream()
                .sorted(Comparator.comparing(DemoCalendarEvent::getDate))
                .collect(Collectors.toList());
    }

    /**
     * デモ用の Slack メッセージ一覧を取得します。
     *
     * @param slackUserId
     *            対象の Slack ユーザー ID（省略時はデモ設定のデフォルト値）
     * @return Slack メッセージのリスト
     */
    public List<DemoMessage> getDemoMessages(String slackUserId) {
        ensureDemoModeEnabled();

        String targetUser = StringUtils.isNotBlank(slackUserId) ? slackUserId : config.demo().slackUserId();
        List<DemoMessage> messages = demoMessageRepository.findBySlackUserId(targetUser);

        return messages.stream()
                .sorted(Comparator.comparing(DemoMessage::getTimestamp))
                .collect(Collectors.toList());
    }

    private void ensureDemoModeEnabled() {
        if (!config.demo().enabled()) {
            throw new DemoModeDisabledException("デモモードが無効です。この機能は利用できません。");
        }
    }
}
