package jp.he23inw3.asset.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;
import jp.he23inw3.asset.domain.gateway.GeminiGateway;
import jp.he23inw3.asset.domain.gateway.SlackGateway;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.DailyLogRepository;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import jp.he23inw3.asset.domain.repository.dto.DailyLogSearchQuery;
import jp.he23inw3.asset.domain.service.DailyLogDomainService;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * 月末の AI パーソナル振り返りレポート作成処理を実行するユースケースクラス。
 * <p>
 * 起動日にて本日の日付が月末日であるかを確認し、有効な全ユーザーを対象として 当月の日報ログを収集し、Gemini に要約レポートの生成を依頼して
 * Slack に投稿します。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class MonthlyReflectionUseCase {

    private final UserSettingRepository userSettingRepository;
    private final DailyLogRepository dailyLogRepository;
    private final DailyLogDomainService dailyLogDomainService;
    private final GeminiGateway geminiGateway;
    private final SlackGateway slackGateway;

    /**
     * 月末振り返りレポート作成のバッチ処理を実行します。
     * <p>
     * 本日が月末日でない場合はスキップされます。
     */
    public void createMonthlyReport() {
        LocalDate today = DateTimeUtil.nowLocalDate();

        // 月末日チェックをドメインサービスに委譲
        if (!dailyLogDomainService.isLastDayOfMonth(today)) {
            log.info(MessageHelper.getMessage("usecase.reflection.skip.notlastday", today));
            return;
        }

        LocalDate start = today.withDayOfMonth(1);
        LocalDate end = today;

        List<UserSetting> activeUsers = userSettingRepository.findAllActive();
        for (UserSetting user : activeUsers) {
            try {
                List<Log> monthlyLogs = dailyLogRepository.findByUserIdAndPeriod(
                        DailyLogSearchQuery.builder()
                                .slackUserId(user.getSlackUserId())
                                .start(start)
                                .end(end)
                                .build());

                if (CollectionUtils.isEmpty(monthlyLogs)) {
                    log.info(MessageHelper.getMessage("usecase.reflection.skip.nologs", user.getSlackUserId()));
                    continue;
                }

                // ログをフォーマット
                String logsText = dailyLogDomainService.formatReflectionLogs(monthlyLogs);

                // Geminiでレポートを生成
                String report = geminiGateway.generateMonthlyReport(logsText);

                // Slackに投稿
                slackGateway.postMessage(user.getSlackUserId(), report);
                log.info(MessageHelper.getMessage("usecase.reflection.sent", user.getSlackUserId()));
            } catch (Exception e) {
                log.error(MessageHelper.getMessage("usecase.reflection.error", user.getSlackUserId()), e);
            }
        }
    }
}
