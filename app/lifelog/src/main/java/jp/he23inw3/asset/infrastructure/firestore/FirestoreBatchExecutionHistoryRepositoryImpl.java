package jp.he23inw3.asset.infrastructure.firestore;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.Query.Direction;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import jp.he23inw3.asset.domain.constant.FirestoreCollectionNames;
import jp.he23inw3.asset.domain.exception.ExternalServiceException;
import jp.he23inw3.asset.domain.model.BatchExecutionHistory;
import jp.he23inw3.asset.domain.model.BatchStatus;
import jp.he23inw3.asset.domain.repository.BatchExecutionHistoryRepository;
import jp.he23inw3.asset.domain.repository.dto.BatchExecutionHistoryQuery;
import jp.he23inw3.asset.domain.util.InstantUtil;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Firestore {@code batch_execution_logs} コレクションの CRUD 実装。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class FirestoreBatchExecutionHistoryRepositoryImpl implements BatchExecutionHistoryRepository {

    private static final String FIELD_ID = "id";
    private static final String FIELD_BATCH_NAME = "batchName";
    private static final String FIELD_STARTED_AT = "startedAt";
    private static final String FIELD_FINISHED_AT = "finishedAt";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_ERROR_MESSAGE = "errorMessage";
    private static final String FIELD_ERROR_STACK_TRACE = "errorStackTrace";
    private static final String FIELD_TRACE_ID = "traceId";

    private final Firestore firestore;

    /**
     * バッチ実行履歴を保存する。
     *
     * @param history 保存対象の履歴ドメインモデル
     */
    @Override
    public void save(BatchExecutionHistory history) {
        try {
            DocumentReference docRef = firestore.collection(FirestoreCollectionNames.BATCH_EXECUTION_HISTORY)
                    .document(history.getId());

            Map<String, Object> data = new HashMap<>();
            data.put(FIELD_ID, history.getId());
            data.put(FIELD_BATCH_NAME, history.getBatchName());
            data.put(FIELD_STARTED_AT,
                    InstantUtil.toEpochSecond(history.getStartedAt().atOffset(ZoneOffset.UTC).toInstant()));
            data.put(FIELD_FINISHED_AT,
                    InstantUtil.toEpochSecond(history.getFinishedAt().atOffset(ZoneOffset.UTC).toInstant()));
            data.put(FIELD_STATUS, history.getStatus().name());
            data.put(FIELD_ERROR_MESSAGE, history.getErrorMessage());
            data.put(FIELD_ERROR_STACK_TRACE, history.getErrorStackTrace());
            data.put(FIELD_TRACE_ID, history.getTraceId());

            docRef.set(data).get();
            log.info(MessageHelper.getMessage("infra.firestore.history.save", history.getBatchName()));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("Firestoreへのバッチ実行履歴の保存に失敗しました。", e);
        }
    }

    /**
     * 検索条件に基づいてバッチ実行履歴の一覧を取得する。
     *
     * @param query 検索条件 DTO
     * @return 該当するバッチ実行履歴の一覧
     */
    @Override
    public List<BatchExecutionHistory> findByQuery(BatchExecutionHistoryQuery query) {
        try {
            String collectionName = FirestoreCollectionNames.BATCH_EXECUTION_HISTORY;
            Query firestoreQuery = firestore.collection(collectionName);

            if (StringUtils.isNotEmpty(query.getBatchId())) {
                firestoreQuery = firestoreQuery.whereEqualTo(FIELD_BATCH_NAME, query.getBatchId());
            }

            if (query.getStatus() != null) {
                firestoreQuery = firestoreQuery.whereEqualTo(FIELD_STATUS, query.getStatus().name());
            }

            if (query.getStart() != null) {
                long startEpoch = query.getStart().toEpochSecond(ZoneOffset.UTC);
                firestoreQuery = firestoreQuery.whereGreaterThanOrEqualTo(FIELD_STARTED_AT, startEpoch);
            }

            if (query.getEnd() != null) {
                long endEpoch = query.getEnd().toEpochSecond(ZoneOffset.UTC);
                firestoreQuery = firestoreQuery.whereLessThanOrEqualTo(FIELD_STARTED_AT, endEpoch);
            }

            // Always order by startedAt DESC
            firestoreQuery = firestoreQuery.orderBy(FIELD_STARTED_AT, Direction.DESCENDING);

            if (query.getOffset() != null && query.getOffset() > 0) {
                firestoreQuery = firestoreQuery.offset(query.getOffset());
            }

            if (query.getLimit() != null && query.getLimit() > 0) {
                firestoreQuery = firestoreQuery.limit(query.getLimit());
            }

            QuerySnapshot querySnapshot = firestoreQuery.get().get();
            List<BatchExecutionHistory> histories = new ArrayList<>();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                histories.add(mapDocumentToHistory(doc));
            }
            return histories;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("Firestoreからのバッチ実行履歴の取得に失敗しました。", e);
        }
    }

    /**
     * 指定されたIDに対応するバッチ実行履歴を取得する。
     *
     * @param id 実行履歴ID
     * @return バッチ実行履歴を格納した {@link Optional}。存在しない場合は {@link Optional#empty()}
     */
    @Override
    public Optional<BatchExecutionHistory> findById(String id) {
        try {
            String collectionName = FirestoreCollectionNames.BATCH_EXECUTION_HISTORY;
            DocumentSnapshot doc = firestore.collection(collectionName).document(id).get().get();
            if (!doc.exists()) {
                return Optional.empty();
            }
            return Optional.of(mapDocumentToHistory(doc));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("Firestoreからのバッチ実行履歴の取得に失敗しました。", e);
        }
    }

    private BatchExecutionHistory mapDocumentToHistory(DocumentSnapshot doc) {
        Long startedAtLong = doc.getLong(FIELD_STARTED_AT);
        LocalDateTime startedAt = startedAtLong != null
                ? LocalDateTime.ofEpochSecond(startedAtLong, 0, ZoneOffset.UTC)
                : null;

        Long finishedAtLong = doc.getLong(FIELD_FINISHED_AT);
        LocalDateTime finishedAt = finishedAtLong != null
                ? LocalDateTime.ofEpochSecond(finishedAtLong, 0, ZoneOffset.UTC)
                : null;

        String statusStr = doc.getString(FIELD_STATUS);
        BatchStatus status = statusStr != null ? BatchStatus.fromValue(statusStr) : null;

        return BatchExecutionHistory.builder()
                .id(doc.getString(FIELD_ID))
                .batchName(doc.getString(FIELD_BATCH_NAME))
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .status(status)
                .errorMessage(doc.getString(FIELD_ERROR_MESSAGE))
                .errorStackTrace(doc.getString(FIELD_ERROR_STACK_TRACE))
                .traceId(doc.getString(FIELD_TRACE_ID))
                .build();
    }
}
