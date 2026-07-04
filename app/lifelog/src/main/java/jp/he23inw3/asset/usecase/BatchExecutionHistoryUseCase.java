package jp.he23inw3.asset.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.model.BatchExecutionHistory;
import jp.he23inw3.asset.domain.repository.BatchExecutionHistoryRepository;
import jp.he23inw3.asset.domain.repository.dto.BatchExecutionHistoryQuery;
import lombok.RequiredArgsConstructor;

/**
 * バッチ実行履歴の取得処理を制御するユースケースクラス。
 */
@ApplicationScoped
@RequiredArgsConstructor
public class BatchExecutionHistoryUseCase {

    private final BatchExecutionHistoryRepository historyRepository;

    /**
     * 指定された検索条件に対応するバッチの実行履歴一覧を取得します。
     *
     * @param query 検索条件 DTO
     * @return 該当するバッチ実行履歴のリスト
     */
    public List<BatchExecutionHistory> getHistories(BatchExecutionHistoryQuery query) {
        return historyRepository.findByQuery(query);
    }

    /**
     * 指定されたIDに対応するバッチ実行履歴の詳細情報を取得します。
     *
     * @param id 実行履歴ID
     * @return バッチ実行履歴のドメインモデル
     * @throws ResourceNotFoundException 実行履歴が見つからない場合
     */
    public BatchExecutionHistory getHistory(String id) {
        return historyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch history not found for " + id));
    }
}
