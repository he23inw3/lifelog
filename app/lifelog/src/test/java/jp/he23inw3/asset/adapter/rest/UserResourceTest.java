package jp.he23inw3.asset.adapter.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jp.he23inw3.asset.adapter.context.ApiContext;
import jp.he23inw3.asset.adapter.dto.AdminRequest;
import jp.he23inw3.asset.adapter.dto.AdminResponse;
import jp.he23inw3.asset.adapter.mapper.AdminMapper;
import jp.he23inw3.asset.domain.exception.ForbiddenException;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.model.AdminUser;
import jp.he23inw3.asset.usecase.AdminUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserResourceTest {

    @Mock
    AdminUseCase adminUseCase;

    @Mock
    AdminMapper adminMapper;

    @Mock
    ApiContext apiContext;

    @InjectMocks
    UserResource target;

    // =========================================================================
    // 管理ユーザー情報取得 (BE-API401)
    // =========================================================================
    @Nested
    @DisplayName("管理ユーザー情報取得")
    class GetAdmin {

        @Test
        @DisplayName("OIDC JWTのメールアドレスで管理ユーザー情報の取得が正常に行われること")
        void getAdmin_Success() {
            String email = "admin@example.com";
            AdminUser mockAdmin = AdminUser.builder().email(email).build();
            AdminResponse mockResponse = AdminResponse.builder().userName("Admin User").build();

            when(apiContext.getAuthenticatedUserId()).thenReturn(email);
            when(adminUseCase.getAdmin(email)).thenReturn(mockAdmin);
            when(adminMapper.toResponse(mockAdmin)).thenReturn(mockResponse);

            AdminResponse response = target.getAdmin();

            assertThat(response).isNotNull();
            assertThat(response.getUserName()).isEqualTo("Admin User");
            verify(apiContext).getAuthenticatedUserId();
            verify(adminUseCase).getAdmin(email);
            verify(adminMapper).toResponse(mockAdmin);
        }

        @Test
        @DisplayName("存在しない管理ユーザーの場合にResourceNotFoundExceptionがスローされること")
        void getAdmin_NotFound_ThrowsResourceNotFoundException() {
            String email = "nonexistent@example.com";

            when(apiContext.getAuthenticatedUserId()).thenReturn(email);
            when(adminUseCase.getAdmin(email)).thenThrow(new ResourceNotFoundException("Not found"));

            assertThatThrownBy(() -> target.getAdmin()).isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================
    // 管理ユーザー情報更新 (BE-API402)
    // =========================================================================
    @Nested
    @DisplayName("管理ユーザー情報更新")
    class UpdateAdmin {

        @Test
        @DisplayName("新規管理ユーザー登録（ブートストラップモード）が正常に行われること")
        void updateAdmin_Bootstrap_Success() {
            String email = "own@example.com";
            AdminRequest request = new AdminRequest();
            request.setUserName("Own User");
            request.setActive(true);

            AdminUser mockAdmin = AdminUser.builder().email(email).userName("Own User").build();
            AdminUser mockSaved = mockAdmin.toBuilder().build();
            AdminResponse mockResponse = AdminResponse.builder().userName("Own User").build();

            when(apiContext.getAuthenticatedUserId()).thenReturn(email);
            when(adminMapper.toDomain(request, email)).thenReturn(mockAdmin);
            when(adminUseCase.saveAdmin(mockAdmin, email)).thenReturn(mockSaved);
            when(adminMapper.toResponse(mockSaved)).thenReturn(mockResponse);

            AdminResponse response = target.updateAdmin(request);

            assertThat(response).isNotNull();
            assertThat(response.getUserName()).isEqualTo("Own User");
            verify(adminUseCase).saveAdmin(mockAdmin, email);
        }

        @Test
        @DisplayName("管理ユーザー更新（通常モード - 有効な管理者による操作）が正常に行われること")
        void updateAdmin_NormalMode_ActiveAdmin_Success() {
            String email = "operator@example.com";
            AdminRequest request = new AdminRequest();

            AdminUser mockAdmin = AdminUser.builder().email(email).build();
            AdminUser mockSaved = mockAdmin.toBuilder().build();
            AdminResponse mockResponse = AdminResponse.builder().userName("Operator").build();

            when(apiContext.getAuthenticatedUserId()).thenReturn(email);
            when(adminMapper.toDomain(request, email)).thenReturn(mockAdmin);
            when(adminUseCase.saveAdmin(mockAdmin, email)).thenReturn(mockSaved);
            when(adminMapper.toResponse(mockSaved)).thenReturn(mockResponse);

            AdminResponse response = target.updateAdmin(request);

            assertThat(response).isNotNull();
            verify(adminUseCase).saveAdmin(mockAdmin, email);
        }

        @Test
        @DisplayName("管理ユーザー更新（通常モード - 無効な管理者による操作）でForbiddenエラーとなること")
        void updateAdmin_NormalMode_InactiveAdmin_Forbidden() {
            String email = "inactive@example.com";
            AdminRequest request = new AdminRequest();
            AdminUser mockAdmin = AdminUser.builder().email(email).build();

            when(apiContext.getAuthenticatedUserId()).thenReturn(email);
            when(adminMapper.toDomain(request, email)).thenReturn(mockAdmin);
            when(adminUseCase.saveAdmin(mockAdmin, email)).thenThrow(new ForbiddenException("Forbidden"));

            assertThatThrownBy(() -> target.updateAdmin(request)).isInstanceOf(ForbiddenException.class);
        }
    }
}
