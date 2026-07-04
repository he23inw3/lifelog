package jp.he23inw3.asset.adapter.mapper;

import jp.he23inw3.asset.adapter.dto.DashboardResponse;
import jp.he23inw3.asset.adapter.dto.MyDashboardResponse;
import jp.he23inw3.asset.domain.model.DashboardStats;
import jp.he23inw3.asset.usecase.UserDashboardUseCase;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * {@link DashboardStats} ドメインモデル ↔ adapter 層 DTO の変換マッパー。
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface DashboardMapper {

    /**
     * ドメインモデルをレスポンス DTO に変換します。
     *
     * @param stats ダッシュボード統計情報のドメインモデル
     * @return 変換後の {@link DashboardResponse}
     */
    DashboardResponse toResponse(DashboardStats stats);

    /**
     * ユーザーの当月統計情報をレスポンス DTO に変換します。
     *
     * @param stats ユーザーダッシュボード統計情報
     * @return 変換後の {@link MyDashboardResponse}
     */
    MyDashboardResponse toMyDashboardResponse(UserDashboardUseCase.UserDashboardStats stats);
}
