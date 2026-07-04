package jp.he23inw3.asset.infrastructure.firestore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.util.List;
import jp.he23inw3.asset.domain.constant.FirestoreCollectionNames;
import jp.he23inw3.asset.domain.model.DemoMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FirestoreDemoMessageRepositoryImplTest {

    @Mock
    Firestore firestore;

    @Mock
    CollectionReference collectionReference;

    @Mock
    Query query;

    @Mock
    QuerySnapshot querySnapshot;

    @Mock
    QueryDocumentSnapshot queryDocumentSnapshot;

    FirestoreDemoMessageRepositoryImpl target;

    @BeforeEach
    void setUp() {
        target = new FirestoreDemoMessageRepositoryImpl(firestore);
    }

    @Nested
    @DisplayName("findBySlackUserIdメソッドのテスト")
    class FindBySlackUserId {

        @Test
        @DisplayName("SlackユーザーIDに紐づくデモメッセージ一覧が正常に返ること")
        void testFindBySlackUserId() throws Exception {
            String slackUserId = "U12345";

            when(firestore.collection(FirestoreCollectionNames.DEMO_SLACK_MESSAGES)).thenReturn(collectionReference);
            when(collectionReference.whereEqualTo("slackUserId", slackUserId)).thenReturn(query);
            when(query.get()).thenReturn(ApiFutures.immediateFuture(querySnapshot));

            when(querySnapshot.getDocuments()).thenReturn(List.of(queryDocumentSnapshot));
            when(queryDocumentSnapshot.getString("slackUserId")).thenReturn(slackUserId);
            when(queryDocumentSnapshot.getString("type")).thenReturn("message");
            when(queryDocumentSnapshot.getString("text")).thenReturn("日報出します");
            when(queryDocumentSnapshot.getLong("timestamp")).thenReturn(1719734400L);

            List<DemoMessage> result = target.findBySlackUserId(slackUserId);

            assertThat(result).hasSize(1);
            DemoMessage message = result.get(0);
            assertThat(message.getSlackUserId()).isEqualTo(slackUserId);
            assertThat(message.getType()).isEqualTo("message");
            assertThat(message.getText()).isEqualTo("日報出します");
            assertThat(message.getTimestamp()).isNotNull();
        }
    }
}
