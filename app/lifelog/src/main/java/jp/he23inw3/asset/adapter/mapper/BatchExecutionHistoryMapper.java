package jp.he23inw3.asset.adapter.mapper;

import java.util.List;
import jp.he23inw3.asset.adapter.dto.BatchExecutionDetailHistoryResponse;
import jp.he23inw3.asset.adapter.dto.BatchExecutionHistoryListResponse;
import jp.he23inw3.asset.domain.model.BatchExecutionHistory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * {@link BatchExecutionHistory} ドメインモデル ↔ レスポンス DTO の変換マッパー。
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface BatchExecutionHistoryMapper {

    /**
     * ドメインモデルをレスポンス DTO に変換します。
     *
     * @param history バッチ実行履歴ドメインモデル
     * @return 変換後のレスポンス DTO
     */
    BatchExecutionHistoryListResponse.BatchExecutionHistory toResponseHistory(BatchExecutionHistory history);

    /**
     * ドメインモデルのリストをレスポンス DTO のリストに変換します。
     *
     * @param histories 変換対象のドメインモデルリスト
     * @return 変換後のレスポンス DTO リスト
     */
    List<BatchExecutionHistoryListResponse.BatchExecutionHistory> toResponseHistoryList(List<BatchExecutionHistory> histories);

    /**
     * ドメインモデルを詳細レスポンス DTO に変換します。
     * @param history バッチ実行履歴ドメインモデル
     * @return 変換後の詳細レスポンス DTO
     */
    BatchExecutionDetailHistoryResponse toDetailResponseHistory(BatchExecutionHistory history);
}
