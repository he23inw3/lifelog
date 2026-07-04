package jp.he23inw3.asset.adapter.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import jp.he23inw3.asset.adapter.dto.LogCalendarSyncResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class LogCalendarSyncMapperTest {

    private final LogCalendarSyncMapper mapper = Mappers.getMapper(LogCalendarSyncMapper.class);

    @Test
    @DisplayName("管理者向けカレンダー同期レスポンスが正しく構築されること")
    void toResponse_ShouldMapCorrectly() {
        LogCalendarSyncResponse response = mapper.toResponse("Synced successfully");

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Synced successfully");
    }
}
