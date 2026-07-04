package jp.he23inw3.asset.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;
import jp.he23inw3.asset.domain.constant.UserMessageConstants;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.gateway.GoogleCalendarGateway;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.model.Sentiment;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.DailyLogRepository;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import jp.he23inw3.asset.domain.service.DailyLogDomainService;
import lombok.RequiredArgsConstructor;

/**
 * 管理画面向けの日報ログ検索・詳細取得・カレンダー再同期を制御するユースケースクラス。
 */
@ApplicationScoped
@RequiredArgsConstructor
public class AdminLogUseCase {

    private final DailyLogRepository dailyLogRepository;

    private final UserSettingRepository userSettingRepository;

    private final GoogleCalendarGateway googleCalendarGateway;

    private final DailyLogDomainService dailyLogDomainService;

    /**
     * 条件に基づいて日報を検索します。
     *
     * @param user ユーザー
     * @param from 開始日
     * @param to 終了日
     * @param holiday 休日フラグ
     * @param sentiment 感情
     * @return 検索結果のリスト
     */
    public List<Log> searchLogs(String user, LocalDate from, LocalDate to, Boolean holiday, Sentiment sentiment) {
        return dailyLogRepository.findByAdminQuery(user, from, to, holiday, sentiment);
    }

    /**
     * 日報ログ詳細を取得します。
     *
     * @param slackUserId Slack ユーザーID
     * @param logDate ログ日付
     * @return 日報ログのドメインモデル
     * @throws ResourceNotFoundException 日報が存在しない場合
     */
    public Log getLogDetail(String slackUserId, LocalDate logDate) {
        return dailyLogRepository.findByUserIdAndDate(slackUserId, logDate)
                .orElseThrow(() -> new ResourceNotFoundException("Daily log not found for " + slackUserId + " on " + logDate));
    }

    /**
     * 日報から Google Calendar の同期を再実行します。
     *
     * @param slackUserId Slack ユーザーID
     * @param logDate ログ日付
     * @throws ResourceNotFoundException ユーザー設定または日報が存在しない場合
     */
    public void syncCalendar(String slackUserId, LocalDate logDate) {
        UserSetting setting = userSettingRepository.findById(slackUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User settings not found for " + slackUserId));

        Log logObj = dailyLogRepository.findByUserIdAndDate(slackUserId, logDate)
                .orElseThrow(() -> new ResourceNotFoundException("Daily log not found for " + slackUserId + " on " + logDate));

        String title = logObj.isHoliday()
                ? UserMessageConstants.WORKFLOW_CALENDAR_TITLE_HOLIDAY
                : UserMessageConstants.WORKFLOW_CALENDAR_TITLE_WORK.replace("{0}", String.valueOf(logObj.getWorkHours()));
        String description = dailyLogDomainService.buildCalendarDescription(logObj.getTasks(), logObj.getDiary(),
                Sentiment.getNameOrNull(logObj.getSentiment()));

        googleCalendarGateway.insertOrUpdateEvent(setting.getGoogleCalendarId(), logDate, title, description);
    }
}
