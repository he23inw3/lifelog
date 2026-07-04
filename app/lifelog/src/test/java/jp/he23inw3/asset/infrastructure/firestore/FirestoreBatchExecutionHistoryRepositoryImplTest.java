package jp.he23inw3.asset.infrastructure.firestore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import jp.he23inw3.asset.domain.constant.FirestoreCollectionNames;
import jp.he23inw3.asset.domain.model.BatchExecutionHistory;
import jp.he23inw3.asset.domain.model.BatchStatus;
import jp.he23inw3.asset.domain.repository.dto.BatchExecutionHistoryQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FirestoreBatchExecutionHistoryRepositoryImplTest {

    @Mock
    Firestore firestore;

    @Mock
    CollectionReference collectionReference;

    @Mock
    DocumentReference documentReference;

    @Mock
    DocumentSnapshot documentSnapshot;

    @Mock
    Query query;

    @Mock
    QuerySnapshot querySnapshot;

    @Mock
    QueryDocumentSnapshot queryDocumentSnapshot;

    FirestoreBatchExecutionHistoryRepositoryImpl target;

    @BeforeEach
    void setUp() {
        target = new FirestoreBatchExecutionHistoryRepositoryImpl(firestore);
    }

    @Nested
    @DisplayName("saveメソッドのテスト")
    class Save {

        @Test
        @DisplayName("バッチ履歴オブジェクトが正常に保存されること")
        void testSave_Success() throws Exception {
            BatchExecutionHistory history = BatchExecutionHistory.builder()
                    .id("uuid-123")
                    .batchName("remind")
                    .startedAt(LocalDateTime.of(2026, 6, 30, 10, 0))
                    .finishedAt(LocalDateTime.of(2026, 6, 30, 10, 5))
                    .status(BatchStatus.SUCCESS)
                    .errorMessage("err")
                    .errorStackTrace("trace")
                    .traceId("trace-123")
                    .build();

            when(firestore.collection(FirestoreCollectionNames.BATCH_EXECUTION_HISTORY))
                    .thenReturn(collectionReference);
            when(collectionReference.document(history.getId())).thenReturn(documentReference);
            when(documentReference.set(anyMap())).thenReturn(ApiFutures.immediateFuture(mock(WriteResult.class)));

            target.save(history);

            verify(documentReference).set(anyMap());
        }
    }

    @Nested
    @DisplayName("findByIdメソッドのテスト")
    class FindById {

        @Test
        @DisplayName("履歴が存在する場合に正常にマッピングされて返ること")
        void testFindById_Exist() throws Exception {
            String id = "uuid-123";
            when(firestore.collection(FirestoreCollectionNames.BATCH_EXECUTION_HISTORY))
                    .thenReturn(collectionReference);
            when(collectionReference.document(id)).thenReturn(documentReference);
            when(documentReference.get()).thenReturn(ApiFutures.immediateFuture(documentSnapshot));

            when(documentSnapshot.exists()).thenReturn(true);
            when(documentSnapshot.getString("id")).thenReturn(id);
            when(documentSnapshot.getString("batchName")).thenReturn("remind");
            when(documentSnapshot.getLong("startedAt"))
                    .thenReturn(LocalDateTime.of(2026, 6, 30, 10, 0).toEpochSecond(ZoneOffset.UTC));
            when(documentSnapshot.getLong("finishedAt"))
                    .thenReturn(LocalDateTime.of(2026, 6, 30, 10, 5).toEpochSecond(ZoneOffset.UTC));
            when(documentSnapshot.getString("status")).thenReturn("SUCCESS");
            when(documentSnapshot.getString("errorMessage")).thenReturn("err");
            when(documentSnapshot.getString("errorStackTrace")).thenReturn("trace");
            when(documentSnapshot.getString("traceId")).thenReturn("trace-123");

            Optional<BatchExecutionHistory> result = target.findById(id);

            assertThat(result).isPresent();
            BatchExecutionHistory h = result.get();
            assertThat(h.getId()).isEqualTo(id);
            assertThat(h.getBatchName()).isEqualTo("remind");
            assertThat(h.getStatus()).isEqualTo(BatchStatus.SUCCESS);
        }

        @Test
        @DisplayName("履歴が存在しない場合にemptyが返ること")
        void testFindById_NotExist() throws Exception {
            String id = "uuid-123";
            when(firestore.collection(FirestoreCollectionNames.BATCH_EXECUTION_HISTORY))
                    .thenReturn(collectionReference);
            when(collectionReference.document(id)).thenReturn(documentReference);
            when(documentReference.get()).thenReturn(ApiFutures.immediateFuture(documentSnapshot));
            when(documentSnapshot.exists()).thenReturn(false);

            Optional<BatchExecutionHistory> result = target.findById(id);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByQueryメソッドのテスト")
    class FindByQuery {

        @Test
        @DisplayName("クエリパラメータに応じた履歴一覧が正常に取得できること")
        void testFindByQuery() throws Exception {
            BatchExecutionHistoryQuery bq = BatchExecutionHistoryQuery.builder()
                    .batchId("remind")
                    .status(BatchStatus.SUCCESS)
                    .start(LocalDateTime.of(2026, 6, 1, 0, 0))
                    .end(LocalDateTime.of(2026, 6, 30, 23, 59))
                    .limit(10)
                    .offset(1)
                    .build();

            when(firestore.collection(FirestoreCollectionNames.BATCH_EXECUTION_HISTORY))
                    .thenReturn(collectionReference);
            when(collectionReference.whereEqualTo(anyString(), any())).thenReturn(query);
            when(query.whereEqualTo(anyString(), any())).thenReturn(query);
            when(query.whereGreaterThanOrEqualTo(anyString(), any())).thenReturn(query);
            when(query.whereLessThanOrEqualTo(anyString(), any())).thenReturn(query);
            when(query.orderBy(anyString(), any(Query.Direction.class))).thenReturn(query);
            when(query.limit(org.mockito.ArgumentMatchers.anyInt())).thenReturn(query);
            when(query.offset(org.mockito.ArgumentMatchers.anyInt())).thenReturn(query);
            when(query.get()).thenReturn(ApiFutures.immediateFuture(querySnapshot));

            when(querySnapshot.getDocuments()).thenReturn(List.of(queryDocumentSnapshot));
            when(queryDocumentSnapshot.getString("id")).thenReturn("uuid-123");
            when(queryDocumentSnapshot.getString("batchName")).thenReturn("remind");
            when(queryDocumentSnapshot.getString("status")).thenReturn("SUCCESS");

            List<BatchExecutionHistory> result = target.findByQuery(bq);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo("uuid-123");
        }
    }
}
