package jp.he23inw3.asset.adapter.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import jp.he23inw3.asset.adapter.dto.CalendarSyncResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class CalendarSyncMapperTest {

    private final CalendarSyncMapper mapper = Mappers.getMapper(CalendarSyncMapper.class);

    @Test
    @DisplayName("成功レスポンスが正しく構築されること")
    void toSuccessResponse_ShouldMapCorrectly() {
        CalendarSyncResponse response = mapper.toSuccessResponse("Synced successfully");

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Synced successfully");
        assertThat(response.getError()).isNull();
    }

    @Test
    @DisplayName("エラーレスポンスが正しく構築されること")
    void toErrorResponse_ShouldMapCorrectly() {
        CalendarSyncResponse response = mapper.toErrorResponse("Sync failed");

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getError()).isEqualTo("Sync failed");
    }
}
