package jp.he23inw3.asset.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import jp.he23inw3.asset.domain.exception.ForbiddenException;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.model.AdminUser;
import jp.he23inw3.asset.domain.repository.AdminUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminUseCaseTest {

    @Mock
    AdminUserRepository adminUserRepository;

    @InjectMocks
    AdminUseCase target;

    @Nested
    @DisplayName("管理者情報取得")
    class GetAdmin {

        @Test
        @DisplayName("管理者情報を取得すること - 成功")
        void getAdmin_Success() {
            // Arrange
            String email = "admin@example.com";
            AdminUser expected = AdminUser.builder().email(email).userName("Admin").active(true).build();
            when(adminUserRepository.findByEmail(email)).thenReturn(Optional.of(expected));

            // Act
            AdminUser actual = target.getAdmin(email);

            // Assert
            assertThat(actual).isEqualTo(expected);
            verify(adminUserRepository).findByEmail(email);
        }

        @Test
        @DisplayName("管理者情報を取得すること - 存在しない場合は例外をスロー")
        void getAdmin_NotFound_ShouldThrowException() {
            // Arrange
            String email = "nonexistent@example.com";
            when(adminUserRepository.findByEmail(email)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> target.getAdmin(email)).isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Admin not found for " + email);
            verify(adminUserRepository).findByEmail(email);
        }
    }

    @Nested
    @DisplayName("管理者情報保存")
    class SaveAdmin {

        @Test
        @DisplayName("管理者情報を新規保存すること (ブートストラップモード - 成功)")
        void saveAdmin_NewUser_Success() {
            // Arrange
            String operator = "operator@example.com";
            AdminUser newAdmin = AdminUser.builder().email("new@example.com").userName("New Admin").active(true)
                    .build();
            when(adminUserRepository.isEmpty()).thenReturn(true);
            when(adminUserRepository.findByEmail(newAdmin.getEmail())).thenReturn(Optional.empty());

            // Act
            AdminUser actual = target.saveAdmin(newAdmin, operator);

            // Assert
            assertThat(actual.getEmail()).isEqualTo(newAdmin.getEmail());
            assertThat(actual.getUserName()).isEqualTo(newAdmin.getUserName());
            assertThat(actual.getCreatedBy()).isEqualTo(operator);
            assertThat(actual.getUpdatedBy()).isEqualTo(operator);
            assertThat(actual.getCreatedAt()).isNotNull();
            assertThat(actual.getUpdatedAt()).isNotNull();
            verify(adminUserRepository).save(any(AdminUser.class));
        }

        @Test
        @DisplayName("管理者情報保存 (通常モード - 操作者が有効な管理者でない場合はForbiddenエラー)")
        void saveAdmin_NormalMode_InactiveOperator_Forbidden() {
            // Arrange
            String operator = "operator@example.com";
            AdminUser newAdmin = AdminUser.builder().email("new@example.com").userName("New Admin").active(true)
                    .build();
            when(adminUserRepository.isEmpty()).thenReturn(false);
            when(adminUserRepository.findByEmail(operator)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> target.saveAdmin(newAdmin, operator))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Nested
    @DisplayName("管理者情報更新")
    class UpdateAdmin {

        @Test
        @DisplayName("管理者情報を更新保存すること")
        void saveAdmin_ExistingUser_Success() {
            // Arrange
            String operator = "operator@example.com";
            Instant originalCreatedAt = Instant.parse("2026-01-01T00:00:00Z");
            AdminUser existing = AdminUser.builder().email("existing@example.com").userName("Existing Admin")
                    .createdBy("original_creator@example.com").createdAt(originalCreatedAt)
                    .updatedBy("original_creator@example.com").updatedAt(originalCreatedAt).active(true).build();

            AdminUser newAdminInfo = AdminUser.builder().email("existing@example.com").userName("Updated Admin Name")
                    .active(false).build();

            when(adminUserRepository.isEmpty()).thenReturn(false);
            when(adminUserRepository.findByEmail(operator))
                    .thenReturn(Optional.of(AdminUser.builder().email(operator).active(true).build()));
            when(adminUserRepository.findByEmail(newAdminInfo.getEmail())).thenReturn(Optional.of(existing));

            // Act
            AdminUser actual = target.saveAdmin(newAdminInfo, operator);

            // Assert
            assertThat(actual.getEmail()).isEqualTo(newAdminInfo.getEmail());
            assertThat(actual.getUserName()).isEqualTo(newAdminInfo.getUserName());
            assertThat(actual.isActive()).isFalse();
            assertThat(actual.getCreatedBy()).isEqualTo("original_creator@example.com");
            assertThat(actual.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(actual.getUpdatedBy()).isEqualTo(operator);
            assertThat(actual.getUpdatedAt()).isAfter(originalCreatedAt);
            verify(adminUserRepository).save(any(AdminUser.class));
        }
    }

    @Nested
    @DisplayName("管理者コレクションが空であるかの判定")
    class IsAdminCollectionEmpty {

        @Test
        @DisplayName("コレクションが空のときにtrueを返すこと")
        void isAdminCollectionEmpty_Empty_ShouldReturnTrue() {
            when(adminUserRepository.isEmpty()).thenReturn(true);
            assertThat(target.isAdminCollectionEmpty()).isTrue();
        }

        @Test
        @DisplayName("コレクションが空でないときにfalseを返すこと")
        void isAdminCollectionEmpty_NotEmpty_ShouldReturnFalse() {
            when(adminUserRepository.isEmpty()).thenReturn(false);
            assertThat(target.isAdminCollectionEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("有効な管理者であるかの判定")
    class IsActiveAdmin {

        @Test
        @DisplayName("アクティブな管理者の場合にtrueを返すこと")
        void isActiveAdmin_Active_ShouldReturnTrue() {
            String email = "active@example.com";
            AdminUser activeUser = AdminUser.builder().email(email).active(true).build();
            when(adminUserRepository.findByEmail(email)).thenReturn(Optional.of(activeUser));

            assertThat(target.isActiveAdmin(email)).isTrue();
        }

        @Test
        @DisplayName("非アクティブな管理者の場合にfalseを返すこと")
        void isActiveAdmin_Inactive_ShouldReturnFalse() {
            String email = "inactive@example.com";
            AdminUser inactiveUser = AdminUser.builder().email(email).active(false).build();
            when(adminUserRepository.findByEmail(email)).thenReturn(Optional.of(inactiveUser));

            assertThat(target.isActiveAdmin(email)).isFalse();
        }

        @Test
        @DisplayName("管理者が存在しない場合にfalseを返すこと")
        void isActiveAdmin_Nonexistent_ShouldReturnFalse() {
            String email = "nonexistent@example.com";
            when(adminUserRepository.findByEmail(email)).thenReturn(Optional.empty());

            assertThat(target.isActiveAdmin(email)).isFalse();
        }
    }
}
