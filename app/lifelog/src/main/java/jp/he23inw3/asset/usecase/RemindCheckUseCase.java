package jp.he23inw3.asset.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import jp.he23inw3.asset.domain.constant.UserMessageConstants;
import jp.he23inw3.asset.domain.gateway.GoogleCalendarGateway;
import jp.he23inw3.asset.domain.gateway.SlackGateway;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.DailyLogRepository;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 毎時実行されるリマインドチェックバッチのユースケースクラス。
 * <p>
 * 平日の指定時刻において、日報がまだ未登録であり、かつカレンダー上で祝日や休暇でないユーザーに対し、 Slack
 * で日報入力の催促リマインドメッセージを送信します。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class RemindCheckUseCase {

    private final UserSettingRepository userSettingRepository;
    private final SlackGateway slackGateway;
    private final GoogleCalendarGateway googleCalendarGateway;
    private final DailyLogRepository dailyLogRepository;

    /**
     * リマインド対象者が存在するかどうかを判定し、必要な場合は Slack 通知を実行します。
     * <p>
     * 土日はスキップされます。
     */
    public void checkAndSendRemind() {
        LocalDate today = DateTimeUtil.nowLocalDate();
        LocalTime now = DateTimeUtil.nowLocalTime();
        String currentHHmm = now.format(DateTimeFormatter.ofPattern("HH:mm"));

        // 土日のスキップ
        DayOfWeek dow = today.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            log.info(MessageHelper.getMessage("usecase.remind.skip.weekend"));
            return;
        }

        List<UserSetting> activeUsers = userSettingRepository.findAllActive();
        for (UserSetting user : activeUsers) {
            try {
                // リマインド要否を判定
                if (!shouldSendRemind(user, today, currentHHmm)) {
                    continue;
                }

                // リマインド送信
                slackGateway.postMessage(user.getSlackUserId(), UserMessageConstants.REMIND_MESSAGE);
                log.info(MessageHelper.getMessage("usecase.remind.sent", user.getSlackUserId()));
            } catch (Exception e) {
                log.error(MessageHelper.getMessage("usecase.remind.error", user.getSlackUserId()), e);
            }
        }
    }

    /**
     * 指定されたユーザーに対し、本日時点でリマインドを送信すべきかどうかを判定します。
     *
     * @param user 判定対象のユーザー設定情報ドメインモデル
     * @param today 判定基準日
     * @param currentHHmm 現在の時刻文字列 (HH:mm)
     * @return リマインドを送信すべき場合は true
     */
    private boolean shouldSendRemind(UserSetting user, LocalDate today, String currentHHmm) {
        // リマインド時刻判定
        if (!currentHHmm.equals(user.getRemindTime())) {
            return false;
        }

        // 祝日または休暇チェック
        if (googleCalendarGateway.isHolidayOrPaidLeave(user.getGoogleCalendarId(), today)) {
            log.info(MessageHelper.getMessage("usecase.remind.skip.holiday", user.getSlackUserId(), today));
            return false;
        }

        // すでに日報があるかチェック
        if (dailyLogRepository.findByUserIdAndDate(user.getSlackUserId(), today).isPresent()) {
            log.info(MessageHelper.getMessage("usecase.remind.already.submitted", user.getSlackUserId()));
            return false;
        }

        return true;
    }
}
