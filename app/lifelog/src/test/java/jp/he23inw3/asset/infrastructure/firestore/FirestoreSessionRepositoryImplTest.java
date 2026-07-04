package jp.he23inw3.asset.infrastructure.firestore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jp.he23inw3.asset.domain.constant.FirestoreCollectionNames;
import jp.he23inw3.asset.domain.model.Session;
import jp.he23inw3.asset.domain.model.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FirestoreSessionRepositoryImplTest {

    @Mock
    Firestore firestore;

    @Mock
    CollectionReference collectionReference;

    @Mock
    DocumentReference documentReference;

    @Mock
    DocumentSnapshot documentSnapshot;

    @Mock
    QuerySnapshot querySnapshot;

    @Mock
    QueryDocumentSnapshot queryDocumentSnapshot;

    FirestoreSessionRepositoryImpl target;

    @BeforeEach
    void setUp() {
        target = new FirestoreSessionRepositoryImpl(firestore);
    }

    @Nested
    @DisplayName("saveメソッドのテスト")
    class Save {

        @Test
        @DisplayName("セッションが正常にFirestoreに保存されること")
        void testSave_Success() throws Exception {
            Session session = Session.builder()
                    .slackUserId("U12345")
                    .status(SessionStatus.AWAITING_CONFIRMATION)
                    .updatedAt(Instant.ofEpochSecond(1000L))
                    .tempData(Map.of("key", "val"))
                    .expiresAt(Instant.ofEpochSecond(2000L))
                    .build();

            when(firestore.collection(FirestoreCollectionNames.USER_SESSIONS)).thenReturn(collectionReference);
            when(collectionReference.document(session.getSlackUserId())).thenReturn(documentReference);
            when(documentReference.set(anyMap())).thenReturn(ApiFutures.immediateFuture(mock(WriteResult.class)));

            target.save(session);

            verify(documentReference).set(anyMap());
        }
    }

    @Nested
    @DisplayName("findByIdメソッドのテスト")
    class FindById {

        @Test
        @DisplayName("セッションが存在する場合に正しくマッピングされて返ること")
        void testFindById_Exist() throws Exception {
            String slackUserId = "U12345";
            when(firestore.collection(FirestoreCollectionNames.USER_SESSIONS)).thenReturn(collectionReference);
            when(collectionReference.document(slackUserId)).thenReturn(documentReference);
            when(documentReference.get()).thenReturn(ApiFutures.immediateFuture(documentSnapshot));

            when(documentSnapshot.exists()).thenReturn(true);
            when(documentSnapshot.getString("slackUserId")).thenReturn(slackUserId);
            when(documentSnapshot.getString("status")).thenReturn("AWAITING_CONFIRMATION");
            when(documentSnapshot.getLong("updatedAt")).thenReturn(1000L);
            when(documentSnapshot.get("tempData")).thenReturn(Map.of("key", "val"));
            when(documentSnapshot.getLong("expiresAt")).thenReturn(2000L);

            Optional<Session> result = target.findById(slackUserId);

            assertThat(result).isPresent();
            Session session = result.get();
            assertThat(session.getSlackUserId()).isEqualTo(slackUserId);
            assertThat(session.getStatus()).isEqualTo(SessionStatus.AWAITING_CONFIRMATION);
            assertThat(session.getTempData()).containsEntry("key", "val");
            assertThat(session.getExpiresAt().getEpochSecond()).isEqualTo(2000L);
        }

        @Test
        @DisplayName("セッションが存在しない場合にemptyが返ること")
        void testFindById_NotExist() throws Exception {
            String slackUserId = "U12345";
            when(firestore.collection(FirestoreCollectionNames.USER_SESSIONS)).thenReturn(collectionReference);
            when(collectionReference.document(slackUserId)).thenReturn(documentReference);
            when(documentReference.get()).thenReturn(ApiFutures.immediateFuture(documentSnapshot));
            when(documentSnapshot.exists()).thenReturn(false);

            Optional<Session> result = target.findById(slackUserId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteメソッドのテスト")
    class Delete {

        @Test
        @DisplayName("セッションが正常に削除されること")
        void testDelete_Success() throws Exception {
            String slackUserId = "U12345";
            when(firestore.collection(FirestoreCollectionNames.USER_SESSIONS)).thenReturn(collectionReference);
            when(collectionReference.document(slackUserId)).thenReturn(documentReference);
            when(documentReference.delete()).thenReturn(ApiFutures.immediateFuture(mock(WriteResult.class)));

            target.delete(slackUserId);

            verify(documentReference).delete();
        }
    }

    @Nested
    @DisplayName("findAllメソッドのテスト")
    class FindAll {

        @Test
        @DisplayName("すべての対話セッションが正常にリストとして取得されること")
        void testFindAll_Success() throws Exception {
            when(firestore.collection(FirestoreCollectionNames.USER_SESSIONS)).thenReturn(collectionReference);
            when(collectionReference.get()).thenReturn(ApiFutures.immediateFuture(querySnapshot));
            when(querySnapshot.getDocuments()).thenReturn(List.of(queryDocumentSnapshot));

            when(queryDocumentSnapshot.getString("slackUserId")).thenReturn("U12345");
            when(queryDocumentSnapshot.getString("status")).thenReturn("AWAITING_CONFIRMATION");
            when(queryDocumentSnapshot.getLong("updatedAt")).thenReturn(1000L);
            when(queryDocumentSnapshot.get("tempData")).thenReturn(Map.of("key", "val"));
            when(queryDocumentSnapshot.getLong("expiresAt")).thenReturn(2000L);

            List<Session> result = target.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSlackUserId()).isEqualTo("U12345");
        }
    }
}
