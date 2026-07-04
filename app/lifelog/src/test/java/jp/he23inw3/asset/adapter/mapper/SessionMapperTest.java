package jp.he23inw3.asset.adapter.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.he23inw3.asset.adapter.dto.SessionListResponse;
import jp.he23inw3.asset.adapter.dto.SessionResetResponse;
import jp.he23inw3.asset.domain.model.Session;
import jp.he23inw3.asset.domain.model.SessionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class SessionMapperTest {

    private final SessionMapper mapper = Mappers.getMapper(SessionMapper.class);

    @Nested
    @DisplayName("SessionからSessionResponseへの変換")
    class ToResponse {

        @Test
        @DisplayName("全フィールドが正しくマッピングされること")
        void toResponse_ShouldMapAllFields() {
            // Arrange
            Instant now = Instant.parse("2026-06-30T10:00:00Z");
            Map<String, String> data = new HashMap<>();
            data.put("key1", "value1");

            Session session = Session.builder()
                    .slackUserId("session-123")
                    .status(SessionStatus.AWAITING_CONFIRMATION)
                    .tempData(data)
                    .updatedAt(now)
                    .build();

            // Act
            SessionListResponse.SessionResponse response = mapper.toResponse(session);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getSlackUserId()).isEqualTo("session-123");
            assertThat(response.getStatus()).isEqualTo("AWAITING_CONFIRMATION");
            assertThat(response.getTempData()).containsEntry("key1", "value1");
            assertThat(response.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 30, 19, 0, 0)); // UTC 10:00 -> JST 19:00
        }

        @Test
        @DisplayName("nullを渡した場合はnullが返ること")
        void toResponse_WithNull_ShouldReturnNull() {
            assertThat(mapper.toResponse(null)).isNull();
        }
    }

    @Nested
    @DisplayName("SessionリストからSessionResponseリストへの変換")
    class ToResponseList {

        @Test
        @DisplayName("リストが正しくマッピングされること")
        void toResponseList_ShouldMapList() {
            // Arrange
            Session s1 = Session.builder().slackUserId("1").status(SessionStatus.WAITING_WORK_HOURS).build();
            Session s2 = Session.builder().slackUserId("2").status(SessionStatus.AWAITING_CONFIRMATION).build();

            // Act
            List<SessionListResponse.SessionResponse> result = mapper.toResponseList(Arrays.asList(s1, s2));

            // Assert
            assertThat(result).isNotNull().hasSize(2);
            assertThat(result.get(0).getSlackUserId()).isEqualTo("1");
            assertThat(result.get(0).getStatus()).isEqualTo("WAITING_WORK_HOURS");
            assertThat(result.get(1).getSlackUserId()).isEqualTo("2");
            assertThat(result.get(1).getStatus()).isEqualTo("AWAITING_CONFIRMATION");
        }

        @Test
        @DisplayName("nullを渡した場合はnullが返ること")
        void toResponseList_WithNull_ShouldReturnNull() {
            assertThat(mapper.toResponseList(null)).isNull();
        }

        @Test
        @DisplayName("空のリストを渡した場合は空のリストが返ること")
        void toResponseList_WithEmptyList_ShouldReturnEmptyList() {
            assertThat(mapper.toResponseList(Collections.emptyList())).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("InstantからLocalDateTimeへのマッピング")
    class MapInstantToLocalDateTime {

        @Test
        @DisplayName("InstantがJSTのLocalDateTimeに正しく変換されること")
        void mapInstantToLocalDateTime_ShouldConvertCorrectly() {
            Instant instant = Instant.parse("2026-06-30T15:00:00Z"); // UTC 15:00 -> JST 24:00 (翌日 00:00)
            LocalDateTime localDateTime = mapper.mapInstantToLocalDateTime(instant);
            assertThat(localDateTime).isEqualTo(LocalDateTime.of(2026, 7, 1, 0, 0, 0));
        }

        @Test
        @DisplayName("nullの場合はnullが返ること")
        void mapInstantToLocalDateTime_WithNull_ShouldReturnNull() {
            assertThat(mapper.mapInstantToLocalDateTime(null)).isNull();
        }
    }

    @Nested
    @DisplayName("SessionResetResponseへのマッピング")
    class ToResetResponse {

        @Test
        @DisplayName("値が正しくマッピングされたSessionResetResponseが構築されること")
        void toResetResponse_ShouldMapCorrectly() {
            SessionResetResponse response = mapper.toResetResponse("U123", "Session has been reset.");

            assertThat(response).isNotNull();
            assertThat(response.getSlackUserId()).isEqualTo("U123");
            assertThat(response.getMessage()).isEqualTo("Session has been reset.");
        }
    }
}
