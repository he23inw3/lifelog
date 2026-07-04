package jp.he23inw3.asset.domain.repository;

import java.util.List;
import java.util.Optional;
import jp.he23inw3.asset.domain.model.BatchExecutionHistory;
import jp.he23inw3.asset.domain.repository.dto.BatchExecutionHistoryQuery;

/**
 * バッチ実行履歴のリポジトリインターフェース。
 */
public interface BatchExecutionHistoryRepository {

    /**
     * バッチ実行履歴を保存する。
     *
     * @param history 保存対象の履歴ドメインモデル
     */
    void save(BatchExecutionHistory history);

    /**
     * 検索条件に基づいてバッチ実行履歴の一覧を取得する。
     *
     * @param query 検索条件 DTO
     * @return 該当するバッチ実行履歴の一覧
     */
    List<BatchExecutionHistory> findByQuery(BatchExecutionHistoryQuery query);

    /**
     * 指定されたIDに対応するバッチ実行履歴を取得する。
     *
     * @param id 実行履歴ID
     * @return バッチ実行履歴を格納した {@link Optional}。存在しない場合は {@link Optional#empty()}
     */
    Optional<BatchExecutionHistory> findById(String id);
}
