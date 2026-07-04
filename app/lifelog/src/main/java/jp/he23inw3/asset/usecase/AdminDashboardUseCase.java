package jp.he23inw3.asset.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jp.he23inw3.asset.domain.model.BatchStatus;
import jp.he23inw3.asset.domain.model.DashboardStats;
import jp.he23inw3.asset.domain.repository.BatchExecutionHistoryRepository;
import jp.he23inw3.asset.domain.repository.DailyLogRepository;
import jp.he23inw3.asset.domain.repository.UserSessionRepository;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import jp.he23inw3.asset.domain.repository.dto.BatchExecutionHistoryQuery;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;

/**
 * 管理画面ダッシュボードの統計情報取得を制御するユースケースクラス。
 */
@ApplicationScoped
@RequiredArgsConstructor
public class AdminDashboardUseCase {

    private final UserSettingRepository userSettingRepository;

    private final DailyLogRepository dailyLogRepository;

    private final UserSessionRepository userSessionRepository;

    private final BatchExecutionHistoryRepository batchHistoryRepository;

    /**
     * ダッシュボードの統計情報を算出・取得します。
     *
     * @return 統計情報ドメインモデル
     */
    public DashboardStats getDashboardStats() {
        int activeUserCount = userSettingRepository.findAllActive().size();

        LocalDate today = DateTimeUtil.nowLocalDate();
        int todayLogCount = (int) dailyLogRepository.countByDate(today);

        int activeSessionCount = userSessionRepository.findAll().size();

        // 本日のバッチエラー件数を取得 (FAILEDかつ本日開始のバッチ)
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(23, 59, 59);
        int todayBatchErrorCount = batchHistoryRepository.findByQuery(
                BatchExecutionHistoryQuery.builder()
                        .status(BatchStatus.FAILED)
                        .start(todayStart)
                        .end(todayEnd)
                        .build())
                .size();

        return DashboardStats.builder()
                .activeUserCount(activeUserCount)
                .todayLogCount(todayLogCount)
                .activeSessionCount(activeSessionCount)
                .todayBatchErrorCount(todayBatchErrorCount)
                .build();
    }
}
