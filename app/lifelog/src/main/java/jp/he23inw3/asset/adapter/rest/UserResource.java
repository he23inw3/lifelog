package jp.he23inw3.asset.adapter.rest;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jp.he23inw3.asset.adapter.constant.ApiPath;
import jp.he23inw3.asset.adapter.constant.ApiTag;
import jp.he23inw3.asset.adapter.context.ApiContext;
import jp.he23inw3.asset.adapter.dto.AdminRequest;
import jp.he23inw3.asset.adapter.dto.AdminResponse;
import jp.he23inw3.asset.adapter.mapper.AdminMapper;
import jp.he23inw3.asset.domain.model.AdminUser;
import jp.he23inw3.asset.usecase.AdminUseCase;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * 管理ユーザー自身の情報取得・登録・更新エンドポイントを提供する REST リソースクラス。
 * <p>
 * ブートストラップモードでは自分自身の初期登録のみ許可。 通常モードでは有効な管理ユーザーのみ操作可能。
 */
@Path(ApiPath.ADMIN_BASE)
@Tag(name = ApiTag.ADMIN)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class UserResource {

    private final ApiContext apiContext;

    private final AdminUseCase adminUseCase;

    private final AdminMapper adminMapper;

    /**
     * OIDC JWT のメールアドレスを用いて、ログイン中の管理ユーザー情報を取得します。
     *
     * @return 管理ユーザー情報レスポンス DTO
     */
    @GET
    @Path("/me")
    @Operation(operationId = "BE-API401", summary = "管理ユーザー情報の取得", description = "OIDC JWT のメールアドレスを用いてログイン中の管理ユーザー情報を取得します")
    public AdminResponse getAdmin() {
        String email = apiContext.getAuthenticatedUserId();
        AdminUser admin = adminUseCase.getAdmin(email);
        return adminMapper.toResponse(admin);
    }

    /**
     * OIDC JWT のメールアドレスを用いて、ログイン中の管理ユーザー情報を新規登録または更新します。
     * <p>
     * ブートストラップモード（管理ユーザーが 0 名）では誰でも自分自身を初期登録可能。 通常モードでは有効な管理ユーザーのみ実行可能。
     *
     * @param request 更新データを含むリクエスト DTO
     * @return 更新後の管理ユーザー情報レスポンス DTO
     */
    @PUT
    @Path("/me")
    @Operation(operationId = "BE-API402", summary = "管理ユーザー情報の更新", description = "OIDC JWT のメールアドレスを用いてログイン中の管理ユーザー情報を新規登録または更新します")
    public AdminResponse updateAdmin(@Valid AdminRequest request) {
        String email = apiContext.getAuthenticatedUserId();

        AdminUser admin = adminMapper.toDomain(request, email);
        AdminUser saved = adminUseCase.saveAdmin(admin, email);
        return adminMapper.toResponse(saved);
    }
}
