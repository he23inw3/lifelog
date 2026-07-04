package jp.he23inw3.asset.adapter.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import jp.he23inw3.asset.adapter.dto.SlackLinkageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class SlackLinkageMapperTest {

    private final SlackLinkageMapper mapper = Mappers.getMapper(SlackLinkageMapper.class);

    @Test
    @DisplayName("Slack連携レスポンスが正しく構築されること")
    void toResponse_ShouldMapCorrectly() {
        SlackLinkageResponse response = mapper.toResponse("Linked successfully");

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Linked successfully");
    }
}
