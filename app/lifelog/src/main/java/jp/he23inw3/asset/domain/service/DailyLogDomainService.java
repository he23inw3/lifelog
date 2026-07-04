package jp.he23inw3.asset.domain.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jp.he23inw3.asset.domain.constant.UserMessageConstants;
import jp.he23inw3.asset.domain.exception.DailyLogValidationException;
import jp.he23inw3.asset.domain.gateway.GoogleCalendarGateway;
import jp.he23inw3.asset.domain.model.GeminiParseResult;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.model.Sentiment;
import jp.he23inw3.asset.domain.repository.DailyLogRepository;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import jp.he23inw3.asset.domain.util.InstantUtil;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 日報（ライフログ）に関するドメインロジックを提供するサービス。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DailyLogDomainService {

    private final GoogleCalendarGateway googleCalendarGateway;

    private final DailyLogRepository dailyLogRepository;

    /**
     * カレンダーや週末判定、ユーザー指定を統合して、本来の休日・休暇状態であるかを判定します。
     *
     * @param date 対象日
     * @param userSpecifiedHoliday ユーザーが明示的に指定した休日フラグ
     * @param calendarId Google カレンダーID
     * @return 休日・休暇である場合は true、平日の場合は false
     */
    public boolean determineHolidayStatus(LocalDate date, boolean userSpecifiedHoliday, String calendarId) {
        if (userSpecifiedHoliday) {
            return true;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return true;
        }
        try {
            return googleCalendarGateway.isHolidayOrPaidLeave(calendarId, date);
        } catch (Exception e) {
            log.warn(MessageHelper.getMessage("service.dailylog.calendar.holiday.error", e.getMessage()));
            return false;
        }
    }

    /**
     * 解析結果から入力情報が不足しているか判定します。
     *
     * @param result Gemini のパース結果
     * @param requestHoliday リクエストで指定された休日フラグ
     * @return 不足している場合は true、十分な場合は false
     */
    public boolean isInputInsufficient(GeminiParseResult result, boolean requestHoliday) {
        if (StringUtils.isBlank(result.getLogDate())) {
            return true;
        }

        boolean isHoliday = requestHoliday || result.isHoliday();

        if (!isHoliday) {
            // 平日（稼働日）: 稼働時間と作業内容が必須
            return result.getWorkHours() <= 0 || StringUtils.isBlank(result.getTasks());
        } else {
            // 休日・休暇日:
            // 作業内容がある場合は稼働時間が必須、稼働時間がある場合は作業内容が必須
            boolean hasTasks = StringUtils.isNotBlank(result.getTasks());
            boolean hasHours = result.getWorkHours() > 0;
            if (hasTasks && !hasHours) {
                return true; // 作業内容はあるが稼働時間がない
            }
            if (hasHours && !hasTasks) {
                return true; // 稼働時間はあるが作業内容がない
            }
            return false;
        }
    }

    /**
     * 不足しているフィールドの説明メッセージを構築します。
     *
     * @param result Gemini のパース結果
     * @param requestHoliday リクエストで指定された休日フラグ
     * @return エラーメッセージ
     */
    public String buildMissingFieldsMessage(GeminiParseResult result, boolean requestHoliday) {
        if (StringUtils.isNotBlank(result.getReplyMessage())) {
            return result.getReplyMessage();
        }
        List<String> missing = new ArrayList<>();
        if (StringUtils.isBlank(result.getLogDate())) {
            missing.add("作業日");
        }

        boolean isHoliday = requestHoliday || result.isHoliday();
        if (!isHoliday) {
            if (result.getWorkHours() <= 0) {
                missing.add("稼働時間");
            }
            if (StringUtils.isBlank(result.getTasks())) {
                missing.add("作業内容");
            }
        } else {
            boolean hasTasks = StringUtils.isNotBlank(result.getTasks());
            boolean hasHours = result.getWorkHours() > 0;
            if (hasTasks && !hasHours) {
                missing.add("稼働時間");
            }
            if (hasHours && !hasTasks) {
                missing.add("作業内容");
            }
        }

        if (CollectionUtils.isNotEmpty(missing)) {
            return String.join("と", missing) + "を入力してください。";
        }
        return UserMessageConstants.WORKFLOW_ASK_HOURS;
    }

    /**
     * Google カレンダーのイベント説明（Description）用のテキストを構築します。
     *
     * @param tasks 業務内容
     * @param diary 日記
     * @param sentiment 感情
     * @return フォーマットされた説明テキスト
     */
    public String buildCalendarDescription(String tasks, String diary, String sentiment) {
        StringBuilder descBuilder = new StringBuilder();
        descBuilder.append("[日報]\n").append(StringUtils.isNotBlank(tasks) ? tasks : "(なし)").append("\n\n[日記]\n")
                .append(StringUtils.isNotBlank(diary) ? diary : "(なし)").append("\n\n[感情]\n")
                .append(StringUtils.isNotBlank(sentiment) ? sentiment : "NEUTRAL");
        return descBuilder.toString();
    }

    /**
     * Gemini の解析結果に基づいて Google カレンダーの予定を登録または更新します。 カレンダー登録での例外は呼び出し元に影響しないよう、内部でキャッチして警告ログを出力します。
     *
     * @param calendarId Google カレンダーID
     * @param date 対象日
     * @param result Gemini 解析結果
     */
    public void registerCalendarEvent(String calendarId, LocalDate date, GeminiParseResult result) {
        try {
            String title = result.isHoliday()
                    ? UserMessageConstants.WORKFLOW_CALENDAR_TITLE_HOLIDAY
                    : UserMessageConstants.WORKFLOW_CALENDAR_TITLE_WORK.replace("{0}",
                            String.valueOf(result.getWorkHours()));
            String description = buildCalendarDescription(result.getTasks(), result.getDiary(),
                    Sentiment.getNameOrNull(result.getSentiment()));
            googleCalendarGateway.insertOrUpdateEvent(calendarId, date, title, description);
        } catch (Exception e) {
            log.warn(MessageHelper.getMessage("service.dailylog.calendar.event.error", e.getMessage()));
        }
    }

    /**
     * 対象日の日報が現在変更可能かどうか（当月内の本日以前であるか）を検証します。 未来日または過去月の日報である場合は例外をスローします。
     *
     * @param targetDate 対象日付
     * @throws DailyLogValidationException 未来日または過去月の日報を変更しようとした場合
     */
    public void validateModificationPeriod(LocalDate targetDate) {
        LocalDate today = DateTimeUtil.nowLocalDate();
        if (targetDate.isAfter(today)) {
            throw new DailyLogValidationException("未来日の日報は登録できません。");
        }

        YearMonth targetMonth = YearMonth.from(targetDate);
        YearMonth currentMonth = YearMonth.from(today);

        if (!targetMonth.equals(currentMonth)) {
            // 本日が1日であり、対象月が前月（先月）である場合は特例として許可する
            boolean isFirstDayOfCurrentMonth = today.getDayOfMonth() == 1;
            YearMonth previousMonth = currentMonth.minusMonths(1);

            if (!(isFirstDayOfCurrentMonth && targetMonth.equals(previousMonth))) {
                throw new DailyLogValidationException("過去月の日報は変更できません。当月内の日報のみ変更可能です。");
            }
        }
    }

    /**
     * 解析結果およびユーザー設定を基に、日報（Log）ドメインモデルを構築・永続化します。 既存の日報が存在する場合は、作成日時（createdAt）を引き継いで更新します。
     *
     * @param slackUserId Slack ユーザーID
     * @param logDate 対象日
     * @param rawText 入力テキスト
     * @param result Gemini 解析結果
     * @param calendarId Google カレンダーID
     * @return 保存された Log ドメインモデル
     */
    public Log saveDailyLog(String slackUserId, LocalDate logDate, String rawText, GeminiParseResult result,
            String calendarId) {
        boolean isActuallyHoliday = determineHolidayStatus(logDate, result.isHoliday(), calendarId);
        Optional<Log> existingOpt = dailyLogRepository.findByUserIdAndDate(slackUserId, logDate);

        Log.LogBuilder builder = existingOpt.map(Log::toBuilder)
                .orElseGet(() -> Log.builder().createdAt(InstantUtil.now()));

        Log logObj = builder
                .slackUserId(slackUserId)
                .logDate(logDate)
                .rawText(rawText)
                .holiday(isActuallyHoliday)
                .tasks(StringUtils.defaultIfBlank(result.getTasks(), ""))
                .workHours(result.getWorkHours())
                .overtimeHours(result.getOvertimeHours())
                .diary(result.getDiary())
                .sentiment(result.getSentiment())
                .updatedAt(InstantUtil.now())
                .build();

        dailyLogRepository.save(logObj);
        return logObj;
    }

    /**
     * 指定された日付がその月の月末日であるかどうかを判定します。
     *
     * @param date 判定対象の日付
     * @return 月末日の場合は true
     */
    public boolean isLastDayOfMonth(LocalDate date) {
        return date != null && date.getDayOfMonth() == date.lengthOfMonth();
    }

    /**
     * 月間ログのリストを、AIレポート生成用のテキストフォーマットに整形します。
     *
     * @param logs 日報ログのリスト
     * @return 整形されたテキスト
     */
    public String formatReflectionLogs(List<Log> logs) {
        if (CollectionUtils.isEmpty(logs)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Log l : logs) {
            sb.append("Date: ").append(l.getLogDate()).append("\n");
            sb.append("Tasks: ").append(StringUtils.defaultIfEmpty(l.getTasks(), "")).append("\n");
            sb.append("Diary: ").append(StringUtils.defaultIfEmpty(l.getDiary(), "")).append("\n\n");
        }
        return sb.toString();
    }
}
