package jp.he23inw3.asset.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.repository.DailyLogRepository;
import jp.he23inw3.asset.domain.repository.dto.DailyLogSearchQuery;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;

/**
 * ログインユーザー自身のダッシュボード統計情報を計算するユースケースクラス。
 * <p>
 * 当月の日報一覧を集計し、マイダッシュボード用の統計値を生成します。
 */
@ApplicationScoped
@RequiredArgsConstructor
public class UserDashboardUseCase {

    private final DailyLogRepository dailyLogRepository;

    /**
     * ログインユーザーの当月統計を取得します。
     *
     * @param slackUserId 統計対象の Slack ユーザーID
     * @return マイダッシュボード統計情報
     */
    public UserDashboardStats getStats(String slackUserId) {
        LocalDate now = DateTimeUtil.nowLocalDate();
        LocalDate monthStart = now.withDayOfMonth(1);

        DailyLogSearchQuery query = DailyLogSearchQuery.builder()
                .slackUserId(slackUserId)
                .start(monthStart)
                .end(now)
                .build();
        List<Log> logs = dailyLogRepository.findByUserIdAndPeriod(query);

        int monthlyLogCount = logs.size();
        double monthlyWorkHours = logs.stream()
                .mapToDouble(l -> l.getWorkHours() != null ? l.getWorkHours() : 0.0)
                .sum();
        double monthlyOvertimeHours = logs.stream()
                .mapToDouble(l -> l.getOvertimeHours() != null ? l.getOvertimeHours() : 0.0)
                .sum();
        Optional<LocalDate> lastLogDate = logs.stream()
                .map(Log::getLogDate)
                .max(Comparator.naturalOrder());

        return new UserDashboardStats(monthlyLogCount, monthlyWorkHours, monthlyOvertimeHours,
                lastLogDate.map(LocalDate::toString).orElse(null));
    }

    /**
     * マイダッシュボード統計情報を保持する値クラス。
     */
    public record UserDashboardStats(
            int monthlyLogCount,
            double monthlyWorkHours,
            double monthlyOvertimeHours,
            String lastLogDate) {
    }
}
