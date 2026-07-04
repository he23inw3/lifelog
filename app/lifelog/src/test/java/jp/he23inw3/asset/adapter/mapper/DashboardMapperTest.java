package jp.he23inw3.asset.adapter.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import jp.he23inw3.asset.adapter.dto.DashboardResponse;
import jp.he23inw3.asset.domain.model.DashboardStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class DashboardMapperTest {

    private final DashboardMapper mapper = Mappers.getMapper(DashboardMapper.class);

    @Nested
    @DisplayName("DashboardStatsからDashboardResponseへの変換")
    class ToResponse {

        @Test
        @DisplayName("統計情報の全フィールドが正しくマッピングされること")
        void toResponse_ShouldMapAllFields() {
            // Arrange
            DashboardStats stats = DashboardStats.builder()
                    .activeUserCount(10)
                    .todayLogCount(5)
                    .activeSessionCount(2)
                    .todayBatchErrorCount(0)
                    .build();

            // Act
            DashboardResponse response = mapper.toResponse(stats);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getActiveUserCount()).isEqualTo(10);
            assertThat(response.getTodayLogCount()).isEqualTo(5);
            assertThat(response.getActiveSessionCount()).isEqualTo(2);
            assertThat(response.getTodayBatchErrorCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("nullを渡した場合はnullが返ること")
        void toResponse_WithNull_ShouldReturnNull() {
            assertThat(mapper.toResponse(null)).isNull();
        }
    }
}
