package jp.he23inw3.asset.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import jp.he23inw3.asset.domain.model.BatchExecutionHistory;
import jp.he23inw3.asset.domain.model.BatchStatus;
import jp.he23inw3.asset.domain.model.DashboardStats;
import jp.he23inw3.asset.domain.model.Session;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.BatchExecutionHistoryRepository;
import jp.he23inw3.asset.domain.repository.DailyLogRepository;
import jp.he23inw3.asset.domain.repository.UserSessionRepository;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import jp.he23inw3.asset.domain.repository.dto.BatchExecutionHistoryQuery;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminDashboardUseCaseTest {

    @Mock
    UserSettingRepository userSettingRepository;

    @Mock
    DailyLogRepository dailyLogRepository;

    @Mock
    UserSessionRepository userSessionRepository;

    @Mock
    BatchExecutionHistoryRepository batchHistoryRepository;

    @InjectMocks
    AdminDashboardUseCase target;

    @Nested
    @DisplayName("ダッシュボード統計情報取得のテスト")
    class GetDashboardStats {

        @Test
        @DisplayName("有効なユーザー数、本日のログ数、セッション数、本日のバッチエラー件数が正しく取得されること")
        void testGetDashboardStats() {
            LocalDate today = LocalDate.of(2026, 6, 30);
            LocalDateTime todayStart = today.atStartOfDay();
            LocalDateTime todayEnd = today.atTime(23, 59, 59);

            try (MockedStatic<DateTimeUtil> dateTimeUtilMock = mockStatic(DateTimeUtil.class)) {
                dateTimeUtilMock.when(DateTimeUtil::nowLocalDate).thenReturn(today);

                when(userSettingRepository.findAllActive()).thenReturn(List.of(mock(UserSetting.class), mock(UserSetting.class)));
                when(dailyLogRepository.countByDate(today)).thenReturn(5L);
                when(userSessionRepository.findAll()).thenReturn(List.of(mock(Session.class)));

                BatchExecutionHistoryQuery expectedQuery = BatchExecutionHistoryQuery.builder()
                        .status(BatchStatus.FAILED)
                        .start(todayStart)
                        .end(todayEnd)
                        .build();
                when(batchHistoryRepository.findByQuery(expectedQuery))
                        .thenReturn(List.of(mock(BatchExecutionHistory.class), mock(BatchExecutionHistory.class), mock(BatchExecutionHistory.class)));

                DashboardStats stats = target.getDashboardStats();

                assertThat(stats.getActiveUserCount()).isEqualTo(2);
                assertThat(stats.getTodayLogCount()).isEqualTo(5);
                assertThat(stats.getActiveSessionCount()).isEqualTo(1);
                assertThat(stats.getTodayBatchErrorCount()).isEqualTo(3);
            }
        }
    }
}
