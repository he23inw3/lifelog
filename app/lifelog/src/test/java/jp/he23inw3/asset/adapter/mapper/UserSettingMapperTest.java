package jp.he23inw3.asset.adapter.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import jp.he23inw3.asset.adapter.dto.UserIntegrationsResponse;
import jp.he23inw3.asset.adapter.dto.UserRegistrationRequest;
import jp.he23inw3.asset.adapter.dto.UserSettingRequest;
import jp.he23inw3.asset.adapter.dto.UserSettingResponse;
import jp.he23inw3.asset.domain.model.UserSetting;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class UserSettingMapperTest {

    private final UserSettingMapper mapper = Mappers.getMapper(UserSettingMapper.class);

    @Nested
    @DisplayName("UserSettingからUserSettingResponseへの変換")
    class ToResponse {

        @Test
        @DisplayName("全フィールドが正しくマッピングされること")
        void toResponse_ShouldMapAllFields() {
            // Arrange
            Instant createdAt = Instant.parse("2026-06-09T10:00:00Z");
            Instant updatedAt = Instant.parse("2026-06-09T11:00:00Z");
            UserSetting setting = UserSetting.builder().slackUserId("U123456").userName("Slack User").remindTime("18:00")
                    .googleCalendarId("cal-123").active(true).googleLinked(true).createdAt(createdAt).updatedAt(updatedAt)
                    .build();

            // Act
            UserSettingResponse response = mapper.toResponse(setting);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getSlackUserId()).isEqualTo("U123456");
            assertThat(response.getUserName()).isEqualTo("Slack User");
            assertThat(response.getRemindTime()).isEqualTo("18:00");
            assertThat(response.getGoogleCalendarId()).isEqualTo("cal-123");
            assertThat(response.isActive()).isTrue();
            assertThat(response.isGoogleLinked()).isTrue();
            assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.ofInstant(createdAt, ZoneId.of("Asia/Tokyo")));
        }

        @Test
        @DisplayName("nullを渡した場合はnullが返ること")
        void toResponse_WithNull_ShouldReturnNull() {
            assertThat(mapper.toResponse(null)).isNull();
        }
    }

    @Nested
    @DisplayName("UserSettingからUserIntegrationsResponseへの変換")
    class ToIntegrationsResponse {

        @Test
        @DisplayName("全フィールドが正しくマッピングされること（Slack連携あり）")
        void toIntegrationsResponse_ShouldMapAllFields_SlackLinked() {
            UserSetting setting = UserSetting.builder()
                    .email("test@example.com")
                    .googleCalendarId("cal-123")
                    .googleLinked(true)
                    .slackUserId("U123456")
                    .build();

            UserIntegrationsResponse response = mapper.toIntegrationsResponse(setting);

            assertThat(response).isNotNull();
            assertThat(response.isGoogleLinked()).isTrue();
            assertThat(response.getGoogleCalendarId()).isEqualTo("cal-123");
            assertThat(response.isSlackLinked()).isTrue();
            assertThat(response.getSlackUserId()).isEqualTo("U123456");
        }

        @Test
        @DisplayName("全フィールドが正しくマッピングされること（Slack連携なし）")
        void toIntegrationsResponse_ShouldMapAllFields_SlackNotLinked() {
            UserSetting setting = UserSetting.builder()
                    .email("test@example.com")
                    .googleCalendarId(null)
                    .googleLinked(false)
                    .slackUserId(null)
                    .build();

            UserIntegrationsResponse response = mapper.toIntegrationsResponse(setting);

            assertThat(response).isNotNull();
            assertThat(response.isGoogleLinked()).isFalse();
            assertThat(response.getGoogleCalendarId()).isNull();
            assertThat(response.isSlackLinked()).isFalse();
            assertThat(response.getSlackUserId()).isNull();
        }

        @Test
        @DisplayName("nullを渡した場合は未連携のレスポンスが返ること")
        void toIntegrationsResponse_WithNull_ShouldReturnDefaultResponse() {
            UserIntegrationsResponse response = mapper.toIntegrationsResponse(null);

            assertThat(response).isNotNull();
            assertThat(response.isGoogleLinked()).isFalse();
            assertThat(response.getGoogleCalendarId()).isNull();
            assertThat(response.isSlackLinked()).isFalse();
            assertThat(response.getSlackUserId()).isNull();
        }
    }

    @Nested
    @DisplayName("RequestオブジェクトからUserSettingへの変換")
    class ToDomain {

        @Test
        @DisplayName("リクエスト情報とSlackユーザーIDが正しくマッピングされること")
        void toDomain_ShouldMapFields() {
            // Arrange
            UserSettingRequest request = new UserSettingRequest();
            request.setUserName("New Name");
            request.setRemindTime("19:00");
            request.setGoogleCalendarId("cal-789");
            request.setActive(false);
            request.setGoogleLinked(true);
            String slackUserId = "U789012";

            // Act
            UserSetting domain = mapper.toDomain(request, slackUserId);

            // Assert
            assertThat(domain).isNotNull();
            assertThat(domain.getSlackUserId()).isEqualTo("U789012");
            assertThat(domain.getUserName()).isEqualTo("New Name");
            assertThat(domain.getRemindTime()).isEqualTo("19:00");
            assertThat(domain.getGoogleCalendarId()).isEqualTo("cal-789");
            assertThat(domain.isActive()).isFalse();
            assertThat(domain.isGoogleLinked()).isTrue();
            assertThat(domain.getCreatedAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
            assertThat(domain.getUpdatedAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("nullを渡した場合はnullが返ること")
        void toDomain_WithNull_ShouldReturnNull() {
            assertThat(mapper.toDomain((UserSettingRequest) null, (String) null)).isNull();
            assertThat(mapper.toDomain((UserRegistrationRequest) null, (String) null)).isNull();
        }
    }

    @Nested
    @DisplayName("ユーティリティメソッド")
    class UtilityMethods {

        @Test
        @DisplayName("InstantからLocalDateTimeへの変換でnullを渡した場合はnullが返ること")
        void mapInstantToLocalDateTime_WithNull_ShouldReturnNull() {
            assertThat(mapper.mapInstantToLocalDateTime(null)).isNull();
        }
    }
}
