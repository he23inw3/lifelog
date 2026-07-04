package jp.he23inw3.asset.adapter.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import jp.he23inw3.asset.adapter.constant.ApiPath;
import jp.he23inw3.asset.adapter.constant.ApiTag;
import jp.he23inw3.asset.adapter.dto.DashboardResponse;
import jp.he23inw3.asset.adapter.dto.UserSettingListResponse;
import jp.he23inw3.asset.adapter.dto.UserSettingResponse;
import jp.he23inw3.asset.adapter.mapper.DashboardMapper;
import jp.he23inw3.asset.adapter.mapper.UserSettingMapper;
import jp.he23inw3.asset.domain.model.DashboardStats;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.usecase.AdminDashboardUseCase;
import jp.he23inw3.asset.usecase.UserSettingUseCase;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * 管理者用ダッシュボードエンドポイントを提供するRESTリソースクラス。
 * 
 */
@Path(ApiPath.ADMIN_BASE)
@Tag(name = ApiTag.ADMIN)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class DashboardResource {

    private final AdminDashboardUseCase adminDashboardUseCase;

    private final DashboardMapper dashboardMapper;

    private final UserSettingUseCase userSettingUseCase;

    private final UserSettingMapper userSettingMapper;

    /**
     * 管理者ダッシュボード用の統計情報を取得します。
     * 
     * @return 管理者ダッシュボード用の統計情報のレスポンスDTO
     */
    @GET
    @Path("/dashboard")
    @Operation(operationId = "BE-API404", summary = "管理者ダッシュボード取得", description = "管理者ダッシュボード用の統計情報を取得します。")
    public DashboardResponse getDashboard() {
        DashboardStats stats = adminDashboardUseCase.getDashboardStats();
        return dashboardMapper.toResponse(stats);
    }

    /**
     * 登録されているすべての利用者一覧を取得します。
     * 
     * @return 利用者一覧のレスポンスDTO
     */
    @GET
    @Path("/users")
    @Operation(operationId = "BE-API405", summary = "利用者一覧取得", description = "登録されているすべての利用者一覧を取得します。")
    public UserSettingListResponse getUsers() {
        List<UserSetting> settings = userSettingUseCase.getAllSettings();
        List<UserSettingResponse> responseList = userSettingMapper.toResponseList(settings);
        return UserSettingListResponse.builder()
                .totalSize(CollectionUtils.size(responseList))
                .userSettings(responseList)
                .build();
    }
}
