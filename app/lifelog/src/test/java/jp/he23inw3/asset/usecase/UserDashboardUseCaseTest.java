package jp.he23inw3.asset.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.repository.DailyLogRepository;
import jp.he23inw3.asset.domain.repository.dto.DailyLogSearchQuery;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserDashboardUseCaseTest {

    @Mock
    DailyLogRepository dailyLogRepository;

    @InjectMocks
    UserDashboardUseCase target;

    @Nested
    @DisplayName("ログインユーザー当月統計取得のテスト")
    class GetStats {

        @Test
        @DisplayName("当月の日報ログから稼働時間や残業時間が集計され正しい結果が返ること")
        void testGetStats() {
            String userId = "U12345";
            LocalDate now = LocalDate.of(2026, 6, 30);
            LocalDate monthStart = LocalDate.of(2026, 6, 1);

            try (MockedStatic<DateTimeUtil> dateTimeUtilMock = mockStatic(DateTimeUtil.class)) {
                dateTimeUtilMock.when(DateTimeUtil::nowLocalDate).thenReturn(now);

                Log log1 = Log.builder()
                        .logDate(LocalDate.of(2026, 6, 10))
                        .workHours(7.5)
                        .overtimeHours(1.0)
                        .build();
                Log log2 = Log.builder()
                        .logDate(LocalDate.of(2026, 6, 20))
                        .workHours(8.0)
                        .overtimeHours(0.0)
                        .build();

                when(dailyLogRepository.findByUserIdAndPeriod(any(DailyLogSearchQuery.class)))
                        .thenReturn(List.of(log1, log2));

                UserDashboardUseCase.UserDashboardStats stats = target.getStats(userId);

                assertThat(stats.monthlyLogCount()).isEqualTo(2);
                assertThat(stats.monthlyWorkHours()).isEqualTo(15.5);
                assertThat(stats.monthlyOvertimeHours()).isEqualTo(1.0);
                assertThat(stats.lastLogDate()).isEqualTo("2026-06-20");

                ArgumentCaptor<DailyLogSearchQuery> captor = ArgumentCaptor.forClass(DailyLogSearchQuery.class);
                verify(dailyLogRepository).findByUserIdAndPeriod(captor.capture());
                DailyLogSearchQuery actualQuery = captor.getValue();
                assertThat(actualQuery.getSlackUserId()).isEqualTo(userId);
                assertThat(actualQuery.getStart()).isEqualTo(monthStart);
                assertThat(actualQuery.getEnd()).isEqualTo(now);
            }
        }

        @Test
        @DisplayName("ログが存在しない場合、集計結果がゼロおよびnullになること")
        void testGetStats_NoLogs() {
            String userId = "U12345";
            LocalDate now = LocalDate.of(2026, 6, 30);

            try (MockedStatic<DateTimeUtil> dateTimeUtilMock = mockStatic(DateTimeUtil.class)) {
                dateTimeUtilMock.when(DateTimeUtil::nowLocalDate).thenReturn(now);

                when(dailyLogRepository.findByUserIdAndPeriod(any(DailyLogSearchQuery.class)))
                        .thenReturn(List.of());

                UserDashboardUseCase.UserDashboardStats stats = target.getStats(userId);

                assertThat(stats.monthlyLogCount()).isEqualTo(0);
                assertThat(stats.monthlyWorkHours()).isEqualTo(0.0);
                assertThat(stats.monthlyOvertimeHours()).isEqualTo(0.0);
                assertThat(stats.lastLogDate()).isNull();
            }
        }
    }
}
