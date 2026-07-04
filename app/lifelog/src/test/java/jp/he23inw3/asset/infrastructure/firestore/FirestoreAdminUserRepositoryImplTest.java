package jp.he23inw3.asset.infrastructure.firestore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import jp.he23inw3.asset.domain.constant.FirestoreCollectionNames;
import jp.he23inw3.asset.domain.exception.ExternalServiceException;
import jp.he23inw3.asset.domain.model.AdminUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FirestoreAdminUserRepositoryImplTest {

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

    FirestoreAdminUserRepositoryImpl target;

    @BeforeEach
    void setUp() {
        target = new FirestoreAdminUserRepositoryImpl(firestore);
    }

    @Nested
    @DisplayName("findByEmailメソッドのテスト")
    class FindByEmail {

        @Test
        @DisplayName("ドキュメントが存在する場合、AdminUserが正常にマッピングされて返ること")
        void testFindByEmail_Exist() throws Exception {
            String email = "admin@example.com";
            when(firestore.collection(FirestoreCollectionNames.ADMIN_USERS)).thenReturn(collectionReference);
            when(collectionReference.document(email)).thenReturn(documentReference);
            when(documentReference.get()).thenReturn(ApiFutures.immediateFuture(documentSnapshot));

            when(documentSnapshot.exists()).thenReturn(true);
            when(documentSnapshot.getId()).thenReturn(email);
            when(documentSnapshot.getString("userName")).thenReturn("管理者");
            when(documentSnapshot.getBoolean("isActive")).thenReturn(true);
            when(documentSnapshot.getString("createdBy")).thenReturn("system");
            when(documentSnapshot.getLong("createdAt")).thenReturn(1000L);
            when(documentSnapshot.getString("updatedBy")).thenReturn("system");
            when(documentSnapshot.getLong("updatedAt")).thenReturn(2000L);

            Optional<AdminUser> result = target.findByEmail(email);

            assertThat(result).isPresent();
            AdminUser user = result.get();
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getUserName()).isEqualTo("管理者");
            assertThat(user.isActive()).isTrue();
            assertThat(user.getCreatedBy()).isEqualTo("system");
            assertThat(user.getCreatedAt().getEpochSecond()).isEqualTo(1000L);
            assertThat(user.getUpdatedBy()).isEqualTo("system");
            assertThat(user.getUpdatedAt().getEpochSecond()).isEqualTo(2000L);
        }

        @Test
        @DisplayName("ドキュメントが存在しない場合、emptyが返ること")
        void testFindByEmail_NotExist() throws Exception {
            String email = "admin@example.com";
            when(firestore.collection(FirestoreCollectionNames.ADMIN_USERS)).thenReturn(collectionReference);
            when(collectionReference.document(email)).thenReturn(documentReference);
            when(documentReference.get()).thenReturn(ApiFutures.immediateFuture(documentSnapshot));
            when(documentSnapshot.exists()).thenReturn(false);

            Optional<AdminUser> result = target.findByEmail(email);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("例外が発生した場合、ExternalServiceExceptionにラップされること")
        void testFindByEmail_Exception() throws Exception {
            String email = "admin@example.com";
            when(firestore.collection(FirestoreCollectionNames.ADMIN_USERS)).thenReturn(collectionReference);
            when(collectionReference.document(email)).thenReturn(documentReference);

            ApiFuture<DocumentSnapshot> mockFuture = mock(ApiFuture.class);
            when(documentReference.get()).thenReturn(mockFuture);
            when(mockFuture.get()).thenThrow(new ExecutionException(new RuntimeException("database error")));

            assertThatThrownBy(() -> target.findByEmail(email))
                    .isInstanceOf(ExternalServiceException.class)
                    .hasMessageContaining("Firestoreからの管理者情報の取得に失敗しました。");
        }
    }

    @Nested
    @DisplayName("saveメソッドのテスト")
    class Save {

        @Test
        @DisplayName("AdminUser情報がFirestoreに正常にセットされること")
        void testSave_Success() throws Exception {
            AdminUser admin = AdminUser.builder()
                    .email("admin@example.com")
                    .userName("管理者")
                    .active(true)
                    .createdBy("system")
                    .createdAt(Instant.ofEpochSecond(1000L))
                    .updatedBy("system")
                    .updatedAt(Instant.ofEpochSecond(2000L))
                    .build();

            when(firestore.collection(FirestoreCollectionNames.ADMIN_USERS)).thenReturn(collectionReference);
            when(collectionReference.document(admin.getEmail())).thenReturn(documentReference);

            WriteResult writeResult = mock(WriteResult.class);
            when(documentReference.set(anyMap())).thenReturn(ApiFutures.immediateFuture(writeResult));

            target.save(admin);

            verify(documentReference).set(anyMap());
        }
    }

    @Nested
    @DisplayName("isEmptyメソッドのテスト")
    class IsEmpty {

        @Test
        @DisplayName("コレクションにデータがない場合、trueを返すこと")
        void testIsEmpty_True() throws Exception {
            when(firestore.collection(FirestoreCollectionNames.ADMIN_USERS)).thenReturn(collectionReference);
            when(collectionReference.limit(1)).thenReturn(query);
            when(query.get()).thenReturn(ApiFutures.immediateFuture(querySnapshot));
            when(querySnapshot.isEmpty()).thenReturn(true);

            assertThat(target.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("コレクションにデータがある場合、falseを返すこと")
        void testIsEmpty_False() throws Exception {
            when(firestore.collection(FirestoreCollectionNames.ADMIN_USERS)).thenReturn(collectionReference);
            when(collectionReference.limit(1)).thenReturn(query);
            when(query.get()).thenReturn(ApiFutures.immediateFuture(querySnapshot));
            when(querySnapshot.isEmpty()).thenReturn(false);

            assertThat(target.isEmpty()).isFalse();
        }
    }
}
