package jp.he23inw3.asset.adapter.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import jp.he23inw3.asset.adapter.dto.AdminRequest;
import jp.he23inw3.asset.adapter.dto.AdminResponse;
import jp.he23inw3.asset.domain.model.AdminUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class AdminMapperTest {

    private final AdminMapper mapper = Mappers.getMapper(AdminMapper.class);

    @Nested
    @DisplayName("AdminUserからAdminResponseへの変換")
    class ToResponse {

        @Test
        @DisplayName("全フィールドが正しくマッピングされること")
        void toResponse_ShouldMapAllFields() {
            // Arrange
            Instant createdAt = Instant.parse("2026-06-09T10:00:00Z");
            Instant updatedAt = Instant.parse("2026-06-09T11:00:00Z");
            AdminUser admin = AdminUser.builder().email("test@example.com").userName("Test User").active(true)
                    .createdBy("creator@example.com").createdAt(createdAt).updatedBy("updater@example.com")
                    .updatedAt(updatedAt).build();

            // Act
            AdminResponse response = mapper.toResponse(admin);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getUserName()).isEqualTo("Test User");
            assertThat(response.isActive()).isTrue();
            assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.ofInstant(createdAt, ZoneId.of("Asia/Tokyo")));
            assertThat(response.getUpdatedAt()).isEqualTo(LocalDateTime.ofInstant(updatedAt, ZoneId.of("Asia/Tokyo")));
        }

        @Test
        @DisplayName("nullを渡した場合はnullが返ること")
        void toResponse_WithNull_ShouldReturnNull() {
            assertThat(mapper.toResponse(null)).isNull();
        }
    }

    @Nested
    @DisplayName("AdminRequestからAdminUserへの変換")
    class ToDomain {

        @Test
        @DisplayName("リクエスト情報とメールアドレスが正しくマッピングされること")
        void toDomain_ShouldMapFields() {
            // Arrange
            AdminRequest request = new AdminRequest();
            request.setUserName("Request User");
            request.setActive(true);
            String email = "request@example.com";

            // Act
            AdminUser domain = mapper.toDomain(request, email);

            // Assert
            assertThat(domain).isNotNull();
            assertThat(domain.getEmail()).isEqualTo("request@example.com");
            assertThat(domain.getUserName()).isEqualTo("Request User");
            assertThat(domain.isActive()).isTrue();
            assertThat(domain.getCreatedBy()).isNull();
            assertThat(domain.getCreatedAt()).isNull();
            assertThat(domain.getUpdatedBy()).isNull();
            assertThat(domain.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("nullを渡した場合はnullが返ること")
        void toDomain_WithNull_ShouldReturnNull() {
            assertThat(mapper.toDomain(null, null)).isNull();
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
