package jp.he23inw3.asset.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import jp.he23inw3.asset.domain.constant.UserMessageConstants;
import jp.he23inw3.asset.domain.exception.DailyLogValidationException;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.gateway.GeminiGateway;
import jp.he23inw3.asset.domain.gateway.SlackGateway;
import jp.he23inw3.asset.domain.model.DayStatus;
import jp.he23inw3.asset.domain.model.GeminiParseResult;
import jp.he23inw3.asset.domain.model.Sentiment;
import jp.he23inw3.asset.domain.model.Session;
import jp.he23inw3.asset.domain.model.SessionStatus;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.UserSessionRepository;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import jp.he23inw3.asset.domain.service.DailyLogDomainService;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import jp.he23inw3.asset.domain.util.InstantUtil;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Slack メッセージを起点としたライフログ登録ワークフロー全体を制御するユースケースクラス。
 * <p>
 * ユーザーとの Slack 上での非同期な対話セッションを管理し、Gemini による解析、
 * カレンダー連携、および永続化処理をオーケストレーションします。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class LifelogWorkflowUseCase {

    /** ユーザー設定管理リポジトリ */
    private final UserSettingRepository userSettingRepository;

    /** 対話型セッション管理リポジトリ */
    private final UserSessionRepository userSessionRepository;

    /** AI解析（Gemini）連携ゲートウェイ */
    private final GeminiGateway geminiGateway;

    /** Slack通知およびインタラクションゲートウェイ */
    private final SlackGateway slackGateway;

    /** 日報関連の共通ドメインサービス */
    private final DailyLogDomainService dailyLogDomainService;

    /**
     * ユーザーから受信したテキストを処理し、ライフログ登録フローを開始または継続します。
     *
     * @param slackUserId 送信元の Slack ユーザーID
     * @param rawText 送信されたテキスト本文
     */
    public void processLifelog(String slackUserId, String rawText) {
        // 1. ユーザー設定とセッションの取得
        UserSetting setting = getUserSetting(slackUserId);
        Optional<Session> sessionOpt = handleExpiredSession(slackUserId);

        // 2. 基準日時・稼働ステータス（休日/平日）の決定
        LocalDateTime now = DateTimeUtil.nowLocalDateTime();
        boolean isHoliday = dailyLogDomainService.determineHolidayStatus(now.toLocalDate(), false,
                setting.getGoogleCalendarId());
        String dayStatus = getDayStatus(now.toLocalDate(), setting.getGoogleCalendarId());
        String mergedText = mergeSessionText(sessionOpt, rawText);

        // 3. Gemini によるテキストの解析
        Optional<GeminiParseResult> resultOpt = parseDailyLogWithGemini(mergedText, now, dayStatus, slackUserId);
        if (resultOpt.isEmpty()) {
            return;
        }
        GeminiParseResult result = resultOpt.get();

        // 4. 日報関連性の判定
        if (!result.isLogRelated()) {
            handleNonLogInput(slackUserId);
            return;
        }

        // 5. 変更制御チェック（過去月の変更制限）
        LocalDate logDate = determineTargetDate(result.getLogDate(), now.toLocalDate());
        try {
            dailyLogDomainService.validateModificationPeriod(logDate);
        } catch (DailyLogValidationException e) {
            slackGateway.postMessage(slackUserId, e.getMessage());
            userSessionRepository.delete(slackUserId);
            return;
        }

        // 6. 入力内容の十分性チェックおよび聞き返し/確認準備
        if (dailyLogDomainService.isInputInsufficient(result, isHoliday)) {
            handleInsufficientInput(slackUserId, mergedText, result);
            return;
        }

        prepareConfirmation(slackUserId, mergedText, result, now.toLocalDate());
    }

    /**
     * ユーザーの承認操作（Slack の確定ボタン押下など）を受け、ライフログの永続化処理を行います。
     *
     * @param slackUserId 操作した Slack ユーザーID
     * @param responseUrl Slack から渡された応答用 Webhook URL
     */
    public void confirmRegistration(String slackUserId, String responseUrl) {
        // 1. セッションの取得および検証
        Session session = getAndValidateSession(slackUserId, responseUrl, SessionStatus.AWAITING_CONFIRMATION);
        if (session == null) {
            return;
        }

        Map<String, String> tempData = session.getTempData();
        UserSetting setting = getUserSetting(slackUserId);

        LocalDate logDate = LocalDate.parse(tempData.get("logDate"));
        String mergedText = tempData.get("rawText");

        // 2. 変更制御チェック: 対象月が当月でない場合は登録・変更不可（安全対策）
        try {
            dailyLogDomainService.validateModificationPeriod(logDate);
        } catch (DailyLogValidationException e) {
            slackGateway.updateMessage(responseUrl, e.getMessage());
            userSessionRepository.delete(slackUserId);
            return;
        }

        // 3. 一時保存データから解析結果を再構築
        GeminiParseResult result = GeminiParseResult.builder()
                .logRelated(true)
                .logDate(tempData.get("logDate"))
                .holiday(Boolean.parseBoolean(tempData.get("isHoliday")))
                .tasks(tempData.get("tasks"))
                .workHours(Double.parseDouble(tempData.get("workHours")))
                .overtimeHours(Double.parseDouble(tempData.getOrDefault("overtimeHours", "0.0")))
                .diary(tempData.get("diary"))
                .sentiment(Sentiment.fromValue(tempData.get("sentiment")))
                .replyMessage(tempData.get("replyMessage"))
                .build();

        // 4. Google カレンダーへのイベント登録/更新
        dailyLogDomainService.registerCalendarEvent(setting.getGoogleCalendarId(), logDate, result);

        // 5. データベース（Firestore / BigQuery）に保存
        dailyLogDomainService.saveDailyLog(slackUserId, logDate, mergedText, result, setting.getGoogleCalendarId());

        // 6. 対話セッションのクリアと Slack 通知
        userSessionRepository.delete(slackUserId);

        String reply = result.getReplyMessage() != null && !result.getReplyMessage().isEmpty()
                ? result.getReplyMessage()
                : UserMessageConstants.WORKFLOW_COMPLETE_REPLY;
        slackGateway.updateMessage(responseUrl, "確定しました。");
        slackGateway.postMessage(slackUserId, reply);
        log.info(MessageHelper.getMessage("usecase.workflow.complete", slackUserId));
    }

    /**
     * ユーザーのキャンセル操作（Slack のキャンセルボタン押下など）を受け、登録処理を中断します。
     *
     * @param slackUserId 操作した Slack ユーザーID
     * @param responseUrl Slack から渡された応答用 Webhook URL
     */
    public void cancelRegistration(String slackUserId, String responseUrl) {
        userSessionRepository.delete(slackUserId);
        slackGateway.updateMessage(responseUrl, "日報の登録をキャンセルしました。");
        log.info(MessageHelper.getMessage("usecase.workflow.cancelled", slackUserId));
    }

    // =========================================================================
    // Private Helper Methods
    // =========================================================================

    /**
     * 指定された Slack ユーザーIDのユーザー設定情報を取得します。
     * 存在しない場合は Slack にエラーメッセージを投稿して例外をスローします。
     *
     * @param slackUserId Slack ユーザーID
     * @return ユーザー設定ドメインモデル
     * @throws ResourceNotFoundException ユーザー設定が見つからない場合
     */
    private UserSetting getUserSetting(String slackUserId) {
        return userSettingRepository.findById(slackUserId).orElseThrow(() -> {
            String msg = UserMessageConstants.WORKFLOW_USER_NOT_FOUND.replace("{0}", slackUserId);
            slackGateway.postMessage(slackUserId, msg);
            return new ResourceNotFoundException("User settings not found for " + slackUserId);
        });
    }

    /**
     * 期限切れのセッションが存在する場合は削除し、Slack通知をして空の Optional を返します。
     * 有効なセッションが存在する場合はそのセッションを返します。
     *
     * @param slackUserId Slack ユーザーID
     * @return 有効なセッション、存在しないまたは期限切れの場合は空の Optional
     */
    private Optional<Session> handleExpiredSession(String slackUserId) {
        Optional<Session> sessionOpt = userSessionRepository.findById(slackUserId);
        if (sessionOpt.isPresent()) {
            Session session = sessionOpt.get();
            if (session.isExpired()) {
                userSessionRepository.delete(slackUserId);
                String expiredMsg = "前回の入力から時間が空いていたため、前回の対話は終了しています。\n"
                        + "このメッセージは新しい記録として受け付けます。\n\n"
                        + "日報や日記として記録したい内容を教えてください。";
                slackGateway.postMessage(slackUserId, expiredMsg);
                return Optional.empty();
            }
        }
        return sessionOpt;
    }

    /**
     * 指定された日付の稼働ステータス判定（祝日・休暇か、平日か）を行い、Gemini 解析用のコンテキスト文字列を返します。
     *
     * @param targetDate 対象日付
     * @param googleCalendarId 連携用 Google カレンダーID
     * @return 「祝日/休暇」または「平日」の文字列
     */
    private String getDayStatus(LocalDate targetDate, String googleCalendarId) {
        boolean isHoliday = dailyLogDomainService.determineHolidayStatus(targetDate, false, googleCalendarId);
        return isHoliday ? DayStatus.HOLIDAY.getValue() : DayStatus.WEEKDAY.getValue();
    }

    /**
     * 未解析のテキストを Gemini API を通じて解析します。
     * 解析に失敗した場合はエラーログを出力し、Slackにエラー通知を行った上で空の Optional を返します。
     *
     * @param mergedText 解析対象のテキスト
     * @param now 基準日時
     * @param dayStatus 解析用コンテキストとしての曜日/日付状態（「平日」など）
     * @param slackUserId Slack ユーザーID
     * @return Gemini による解析結果オブジェクト。失敗時は空の Optional
     */
    private Optional<GeminiParseResult> parseDailyLogWithGemini(String mergedText, LocalDateTime now, String dayStatus,
            String slackUserId) {
        try {
            return Optional.of(geminiGateway.parse(mergedText, now, dayStatus));
        } catch (Exception e) {
            log.error(MessageHelper.getMessage("infra.gemini.parse.error") + ": " + e.getMessage(), e);
            slackGateway.postMessage(slackUserId, MessageHelper.getMessage("usecase.workflow.parse.error"));
            return Optional.empty();
        }
    }

    /**
     * 指定された Slack ユーザーIDのセッションを取得し、ステータスと期限の検証を行います。
     * 無効な場合はエラーメッセージを返信し、期限切れの場合はセッションをクリアした上で null を返します。
     *
     * @param slackUserId Slack ユーザーID
     * @param responseUrl Slack 応答用 Webhook URL
     * @param expectedStatus 期待するセッションステータス
     * @return 検証済みのセッション。無効な場合は null
     */
    private Session getAndValidateSession(String slackUserId, String responseUrl, SessionStatus expectedStatus) {
        Session session = userSessionRepository.findById(slackUserId).orElse(null);
        if (session == null || session.isExpired() || session.getStatus() != expectedStatus) {
            if (session != null && session.isExpired()) {
                userSessionRepository.delete(slackUserId);
            }
            slackGateway.updateMessage(responseUrl, "セッションが見つからないか、期限切れです。もう一度やり直してください。");
            return null;
        }
        return session;
    }

    /**
     * 既存のセッションが存在する場合、そのテキストと新規入力テキストをマージします。
     *
     * @param sessionOpt 既存セッションの Optional オブジェクト
     * @param rawText 今回新規入力テキスト
     * @return マージされた入力テキスト
     */
    private String mergeSessionText(Optional<Session> sessionOpt, String rawText) {
        if (sessionOpt.isPresent()) {
            Session session = sessionOpt.get();
            String prevText = session.getTempData().get("rawText");
            if (StringUtils.isNotBlank(prevText)) {
                return prevText + "\n" + rawText;
            }
        }
        return rawText;
    }

    /**
     * ライフログに無関係な入力の処理を行います。 ログを出力し、対話セッションをクリアします。
     *
     * @param slackUserId Slack ユーザーID
     */
    private void handleNonLogInput(String slackUserId) {
        log.info(MessageHelper.getMessage("usecase.workflow.skip.notlogrelated", slackUserId));
        userSessionRepository.delete(slackUserId);
    }

    /**
     * 入力情報不足時の処理を行います。 セッションにテキストを一時保存し、Slack へ聞き返しメッセージを送信します。
     *
     * @param slackUserId Slack ユーザーID
     * @param mergedText ここまでの入力テキスト全体
     * @param result Gemini のパース結果
     */
    private void handleInsufficientInput(String slackUserId, String mergedText, GeminiParseResult result) {
        log.info(MessageHelper.getMessage("usecase.workflow.insufficient", slackUserId));

        Map<String, String> tempData = new HashMap<>();
        tempData.put("rawText", mergedText);
        Session newSession = Session.builder()
                .slackUserId(slackUserId)
                .status(SessionStatus.WAITING_WORK_HOURS)
                .updatedAt(Instant.now())
                .expiresAt(Instant.now().plus(60, ChronoUnit.MINUTES))
                .tempData(tempData)
                .build();
        userSessionRepository.save(newSession);

        String reply = result.getReplyMessage();
        if (StringUtils.isBlank(reply)) {
            reply = dailyLogDomainService.buildMissingFieldsMessage(result, false);
        }
        slackGateway.postMessage(slackUserId, reply);
    }

    /**
     * ライフログの登録および完了プロセスの全体を処理します。
     *
     * @param slackUserId Slack ユーザーID
     * @param mergedText マージ済みのテキスト
     * @param result Gemini のパース結果
     * @param today 本日の日付
     */
    private void prepareConfirmation(String slackUserId, String mergedText, GeminiParseResult result, LocalDate today) {
        LocalDate logDate = determineTargetDate(result.getLogDate(), today);

        Map<String, String> tempData = new HashMap<>();
        tempData.put("rawText", StringUtils.defaultIfEmpty(mergedText, ""));
        tempData.put("tasks", StringUtils.defaultIfEmpty(result.getTasks(), ""));
        tempData.put("workHours", String.valueOf(result.getWorkHours()));
        tempData.put("overtimeHours", String.valueOf(result.getOvertimeHours()));
        tempData.put("diary", StringUtils.defaultIfEmpty(result.getDiary(), ""));
        tempData.put("sentiment", Sentiment.getNameOrDefault(result.getSentiment()));
        tempData.put("logDate", logDate.toString());
        tempData.put("isHoliday", String.valueOf(result.isHoliday()));
        tempData.put("replyMessage", StringUtils.defaultIfEmpty(result.getReplyMessage(), ""));

        Instant now = InstantUtil.now();
        Session session = Session.builder()
                .slackUserId(slackUserId)
                .status(SessionStatus.AWAITING_CONFIRMATION)
                .updatedAt(now)
                .expiresAt(now.plus(60, ChronoUnit.MINUTES))
                .tempData(tempData).build();
        userSessionRepository.save(session);

        slackGateway.postConfirmationMessage(slackUserId, logDate.toString(), result.getTasks(), result.getWorkHours(),
                result.getDiary(), result.isHoliday());
        log.info(MessageHelper.getMessage("usecase.workflow.initiated", slackUserId));
    }

    /**
     * Gemini の解析結果に含まれる日付情報を基に、日報の対象日付を決定します。
     * 解析結果に有効な日付が存在しない場合は、基準日（本日日付）を返します。
     *
     * @param logDateStr ログ日付文字列
     * @param defaultDate デフォルトの日付（今日）
     * @return 決定されたログ日付の LocalDate オブジェクト
     */
    private LocalDate determineTargetDate(String logDateStr, LocalDate defaultDate) {
        if (StringUtils.isNotBlank(logDateStr)) {
            try {
                return LocalDate.parse(logDateStr);
            } catch (Exception e) {
                log.warn(MessageHelper.getMessage("usecase.workflow.parse.date.fallback", logDateStr));
            }
        }
        return defaultDate;
    }
}
