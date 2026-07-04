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
import com.google.cloud.firestore.WriteResult;
import java.time.Instant;
import java.util.Optional;
import jp.he23inw3.asset.domain.constant.FirestoreCollectionNames;
import jp.he23inw3.asset.domain.model.SlackLinkageToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FirestoreSlackLinkageTokenRepositoryImplTest {

    @Mock
    Firestore firestore;

    @Mock
    CollectionReference collectionReference;

    @Mock
    DocumentReference documentReference;

    @Mock
    DocumentSnapshot documentSnapshot;

    FirestoreSlackLinkageTokenRepositoryImpl target;

    @BeforeEach
    void setUp() {
        target = new FirestoreSlackLinkageTokenRepositoryImpl(firestore);
    }

    @Nested
    @DisplayName("saveメソッドのテスト")
    class Save {

        @Test
        @DisplayName("連携トークン情報が正常に保存されること")
        void testSave() throws Exception {
            SlackLinkageToken token = SlackLinkageToken.builder()
                    .token("tok-123")
                    .slackUserId("U123")
                    .expiresAt(Instant.ofEpochSecond(2000L))
                    .build();

            when(firestore.collection(FirestoreCollectionNames.SLACK_LINKAGE_TOKENS)).thenReturn(collectionReference);
            when(collectionReference.document("tok-123")).thenReturn(documentReference);
            when(documentReference.set(anyMap())).thenReturn(ApiFutures.immediateFuture(mock(WriteResult.class)));

            target.save(token);

            verify(documentReference).set(anyMap());
        }
    }

    @Nested
    @DisplayName("findByTokenメソッドのテスト")
    class FindByToken {

        @Test
        @DisplayName("トークンが存在する場合に正常にマッピングされて返ること")
        void testFindByToken_Exist() throws Exception {
            String token = "tok-123";
            when(firestore.collection(FirestoreCollectionNames.SLACK_LINKAGE_TOKENS)).thenReturn(collectionReference);
            when(collectionReference.document(token)).thenReturn(documentReference);
            when(documentReference.get()).thenReturn(ApiFutures.immediateFuture(documentSnapshot));

            when(documentSnapshot.exists()).thenReturn(true);
            when(documentSnapshot.getString("slackUserId")).thenReturn("U123");
            when(documentSnapshot.getLong("expiresAt")).thenReturn(2000L);

            Optional<SlackLinkageToken> result = target.findByToken(token);

            assertThat(result).isPresent();
            SlackLinkageToken t = result.get();
            assertThat(t.getToken()).isEqualTo(token);
            assertThat(t.getSlackUserId()).isEqualTo("U123");
            assertThat(t.getExpiresAt().getEpochSecond()).isEqualTo(2000L);
        }

        @Test
        @DisplayName("トークンが存在しない場合にemptyが返ること")
        void testFindByToken_NotExist() throws Exception {
            String token = "tok-123";
            when(firestore.collection(FirestoreCollectionNames.SLACK_LINKAGE_TOKENS)).thenReturn(collectionReference);
            when(collectionReference.document(token)).thenReturn(documentReference);
            when(documentReference.get()).thenReturn(ApiFutures.immediateFuture(documentSnapshot));
            when(documentSnapshot.exists()).thenReturn(false);

            Optional<SlackLinkageToken> result = target.findByToken(token);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteメソッドのテスト")
    class Delete {

        @Test
        @DisplayName("トークンが正常に削除されること")
        void testDelete() throws Exception {
            String token = "tok-123";
            when(firestore.collection(FirestoreCollectionNames.SLACK_LINKAGE_TOKENS)).thenReturn(collectionReference);
            when(collectionReference.document(token)).thenReturn(documentReference);
            when(documentReference.delete()).thenReturn(ApiFutures.immediateFuture(mock(WriteResult.class)));

            target.delete(token);

            verify(documentReference).delete();
        }
    }
}
