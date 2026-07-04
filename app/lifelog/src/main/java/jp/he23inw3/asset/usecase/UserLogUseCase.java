package jp.he23inw3.asset.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import jp.he23inw3.asset.domain.constant.UserMessageConstants;
import jp.he23inw3.asset.domain.exception.DailyLogValidationException;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.gateway.GeminiGateway;
import jp.he23inw3.asset.domain.gateway.GoogleCalendarGateway;
import jp.he23inw3.asset.domain.model.DayStatus;
import jp.he23inw3.asset.domain.model.GeminiParseResult;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.model.Sentiment;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.DailyLogRepository;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import jp.he23inw3.asset.domain.repository.dto.DailyLogSearchQuery;
import jp.he23inw3.asset.domain.service.DailyLogDomainService;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * ログインユーザー自身の日報ログ操作（登録、更新、取得、カレンダー連携）を制御するユースケースクラス。
 * <p>
 * 入力された未加工テキストから Gemini API を用いて業務内容や感情を自動抽出し、
 * Google カレンダーおよび BigQuery/Firestore データベースへの永続化処理をオーケストレーションします。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class UserLogUseCase {

    /** 日報ログ永続化リポジトリ */
    private final DailyLogRepository dailyLogRepository;

    /** ユーザー設定管理リポジトリ */
    private final UserSettingRepository userSettingRepository;

    /** AI解析（Gemini）連携ゲートウェイ */
    private final GeminiGateway geminiGateway;

    /** 日報関連の共通ドメインサービス */
    private final DailyLogDomainService dailyLogDomainService;

    /** Googleカレンダー連携ゲートウェイ */
    private final GoogleCalendarGateway googleCalendarGateway;

    /**
     * 未解析のテキストを元に、新しい日報を解析・登録します。
     * <p>
     * 既に該当日付に日報が存在し、かつその日付が過去月（当月以外）の場合は変更とみなして例外をスローします。
     *
     * @param slackUserId ユーザーID
     * @param rawText テキスト
     * @param holiday 休日フラグ
     * @return 解析および登録された日報ドメインモデル
     * @throws DailyLogValidationException 過去月の日報を更新しようとした場合、または入力内容に不足がある場合
     */
    public Log createLog(String slackUserId, String rawText, boolean holiday) {
        // ユーザー設定を取得
        UserSetting setting = getUserSetting(slackUserId);

        // 基準日付の決定
        LocalDate baseDate = DateTimeUtil.nowLocalDate();

        // 祝日・休暇状況を取得（Gemini 解析のコンテキストとして使用）
        String dayStatus = getDayStatus(baseDate, holiday, setting.getGoogleCalendarId());

        // Gemini で rawText を解析
        GeminiParseResult result = parseDailyLogWithGemini(rawText, baseDate, holiday, dayStatus);

        // 対象日を決定（解析結果の日付があればそれを使用し、なければ baseDate）
        LocalDate targetDate = determineTargetDate(result, baseDate);

        // 変更制御チェック: 対象月が当月でない場合は登録・変更不可
        dailyLogDomainService.validateModificationPeriod(targetDate);

        // バリデーション実行 & 日付補完
        GeminiParseResult validatedResult = validateAndInterpolateParseResult(result, targetDate, holiday);

        // Calendar にイベント登録/更新
        dailyLogDomainService.registerCalendarEvent(setting.getGoogleCalendarId(), targetDate, validatedResult);

        // Firestore / BigQuery に保存
        Log logObj = dailyLogDomainService.saveDailyLog(slackUserId, targetDate, rawText, validatedResult, setting.getGoogleCalendarId());
        log.info(MessageHelper.getMessage("usecase.userlog.register.success", slackUserId, targetDate));
        return logObj;
    }

    /**
     * 未解析のテキストを元に、新しい日報を解析します（登録・同期は行いません）。
     *
     * @param slackUserId ユーザーID
     * @param rawText テキスト
     * @param holiday 休日フラグ
     * @return 解析およびバリデーションされた日報ドメインモデル
     * @throws DailyLogValidationException 入力内容に不足がある場合
     */
    public Log analyzeLog(String slackUserId, String rawText, boolean holiday) {
        // ユーザー設定を取得
        UserSetting setting = getUserSetting(slackUserId);

        // 基準日付の決定
        LocalDate baseDate = DateTimeUtil.nowLocalDate();

        // 祝日・休暇状況を取得（Gemini 解析のコンテキストとして使用）
        String dayStatus = getDayStatus(baseDate, holiday, setting.getGoogleCalendarId());

        // Gemini で rawText を解析
        GeminiParseResult result = parseDailyLogWithGemini(rawText, baseDate, holiday, dayStatus);

        // 対象日を決定（解析結果の日付があればそれを使用し、なければ baseDate）
        LocalDate targetDate = determineTargetDate(result, baseDate);

        // 変更制御チェック: 対象月が当月でない場合は登録・変更不可
        dailyLogDomainService.validateModificationPeriod(targetDate);

        // バリデーション実行 & 日付補完
        GeminiParseResult validatedResult = validateAndInterpolateParseResult(result, targetDate, holiday);

        // 保存はせずに、解析結果を詰め込んだ Log オブジェクトを構築して返す
        return Log.builder()
                .slackUserId(slackUserId)
                .logDate(targetDate)
                .rawText(rawText)
                .holiday(validatedResult.isHoliday())
                .tasks(validatedResult.getTasks())
                .workHours(validatedResult.getWorkHours())
                .overtimeHours(validatedResult.getOvertimeHours())
                .diary(validatedResult.getDiary())
                .sentiment(validatedResult.getSentiment())
                .build();
    }

    /**
     * 指定された検索条件（期間）に一致するユーザー自身の日報ログ一覧を取得します。
     *
     * @param query ユーザー検索クエリDTO
     * @return 該当する日報ログのリスト
     */
    public List<Log> getLogs(DailyLogSearchQuery query) {
        return dailyLogRepository.findByUserIdAndPeriod(query);
    }

    /**
     * 指定されたユーザーIDと日付に対応する特定の日報ログを取得します。
     *
     * @param slackUserId Slack ユーザーID
     * @param logDate 対象日付
     * @return 日報ログドメインモデル
     * @throws ResourceNotFoundException 該当日付の日報が存在しない場合
     */
    public Log getLog(String slackUserId, LocalDate logDate) {
        return dailyLogRepository.findByUserIdAndDate(slackUserId, logDate).orElseThrow(
                () -> new ResourceNotFoundException("Daily log not found for " + slackUserId + " on " + logDate));
    }

    /**
     * 指定された日付の日報を Google カレンダーに再同期します。
     * <p>
     * 日報が存在しない場合は {@link ResourceNotFoundException} をスローします。
     * カレンダー側の障害は呼び出し元に伝播させます。
     *
     * @param slackUserId ログインユーザーの Slack ユーザーID
     * @param logDate 対象日付
     * @throws ResourceNotFoundException ユーザー設定または日報が存在しない場合
     */
    public void syncCalendar(String slackUserId, LocalDate logDate) {
        UserSetting setting = userSettingRepository.findById(slackUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User settings not found for " + slackUserId));

        Log logObj = dailyLogRepository.findByUserIdAndDate(slackUserId, logDate)
                .orElseThrow(() -> new ResourceNotFoundException("Daily log not found for " + slackUserId + " on " + logDate));

        String title = logObj.isHoliday()
                ? UserMessageConstants.WORKFLOW_CALENDAR_TITLE_HOLIDAY
                : UserMessageConstants.WORKFLOW_CALENDAR_TITLE_WORK.replace("{0}",
                        String.valueOf(logObj.getWorkHours()));
        String description = dailyLogDomainService.buildCalendarDescription(logObj.getTasks(), logObj.getDiary(),
                Sentiment.getNameOrNull(logObj.getSentiment()));

        googleCalendarGateway.insertOrUpdateEvent(setting.getGoogleCalendarId(), logDate, title, description);
        log.info(MessageHelper.getMessage("usecase.userlog.calendar.sync.success", slackUserId, logDate));
    }

    // =========================================================================
    // Private Helper Methods
    // =========================================================================

    /**
     * Gemini の解析結果に含まれる日付情報を基に、日報の対象日付を決定します。
     * 解析結果に有効な日付が存在しない場合は、基準日（本日日付）を返します。
     *
     * @param result Gemini 解析結果
     * @param baseDate 基準日付
     * @return 確定された LocalDate オブジェクト
     */
    private LocalDate determineTargetDate(GeminiParseResult result, LocalDate baseDate) {
        if (StringUtils.isNotBlank(result.getLogDate())) {
            try {
                return LocalDate.parse(result.getLogDate());
            } catch (Exception e) {
                return baseDate;
            }
        }
        return baseDate;
    }

    /**
     * 指定された日付の稼働ステータス判定（祝日・休暇か、平日か）を行い、Gemini 解析用のコンテキスト文字列を返します。
     *
     * @param targetDate 対象日付
     * @param isHolidayCommand コマンドで指定された休日フラグ
     * @param googleCalendarId 連携用 Google カレンダーID
     * @return 「祝日/休暇」または「平日」の文字列
     */
    private String getDayStatus(LocalDate targetDate, boolean isHolidayCommand, String googleCalendarId) {
        boolean isHoliday = dailyLogDomainService.determineHolidayStatus(targetDate, isHolidayCommand,
                googleCalendarId);
        return isHoliday ? DayStatus.HOLIDAY.getValue() : DayStatus.WEEKDAY.getValue();
    }

    /**
     * ユーザーIDに紐づくユーザー設定情報を取得します。
     *
     * @param slackUserId Slack ユーザーID
     * @return ユーザー設定モデル
     * @throws ResourceNotFoundException 設定が見つからない場合
     */
    private UserSetting getUserSetting(String slackUserId) {
        return userSettingRepository.findById(slackUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User settings not found for " + slackUserId));
    }

    /**
     * 未解析のテキストを Gemini API を通じて解析します。
     * 解析に失敗した場合は警告をログに出力し、中立（NEUTRAL）感情と稼働時間 0 のフォールバックオブジェクトを返します。
     *
     * @param rawText 解析対象の未加工テキスト
     * @param targetDate 対象日付
     * @param holiday 休日フラグ
     * @param dayStatus 解析用コンテキストとしての曜日/日付状態（「平日」など）
     * @return Gemini による解析結果オブジェクト
     */
    private GeminiParseResult parseDailyLogWithGemini(String rawText, LocalDate targetDate, boolean holiday, String dayStatus) {
        LocalDateTime now = DateTimeUtil.nowLocalDateTime();
        try {
            return geminiGateway.parse(rawText, now, dayStatus);
        } catch (Exception e) {
            log.warn(MessageHelper.getMessage("usecase.userlog.parse.error", e.getMessage()));
            return GeminiParseResult.builder()
                    .logRelated(true)
                    .logDate(targetDate.toString())
                    .holiday(holiday)
                    .workHours(0.0)
                    .overtimeHours(0.0)
                    .sentiment(Sentiment.NEUTRAL)
                    .build();
        }
    }

    /**
     * Gemini の解析結果に対して、日報として最低限必要な項目が揃っているかをバリデーションし、日付補完を行います。
     *
     * @param result Gemini 解析結果
     * @param targetDate 登録対象日付
     * @param requestHoliday リクエスト時の休日フラグ
     * @return 日付が補完された解析結果オブジェクト
     * @throws DailyLogValidationException 日報と無関係な入力である場合、または必須入力項目が不足している場合
     */
    private GeminiParseResult validateAndInterpolateParseResult(GeminiParseResult result, LocalDate targetDate, boolean requestHoliday) {
        GeminiParseResult validatedResult = result;
        if (StringUtils.isBlank(validatedResult.getLogDate())) {
            validatedResult = validatedResult.toBuilder().logDate(targetDate.toString()).build();
        }
        if (!validatedResult.isLogRelated()) {
            throw new DailyLogValidationException("日報に関係のない入力です。作業内容や稼働時間を入力してください。");
        }
        if (dailyLogDomainService.isInputInsufficient(validatedResult, requestHoliday)) {
            throw new DailyLogValidationException(
                    dailyLogDomainService.buildMissingFieldsMessage(validatedResult, requestHoliday));
        }
        return validatedResult;
    }
}
