package jp.he23inw3.asset.infrastructure.firestore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import com.google.cloud.firestore.Transaction;
import com.google.cloud.firestore.WriteResult;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import jp.he23inw3.asset.domain.constant.FirestoreCollectionNames;
import jp.he23inw3.asset.domain.model.UserSetting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FirestoreSettingRepositoryImplTest {

    @Mock
    Firestore firestore;

    @Mock
    CollectionReference settingsCollection;

    @Mock
    CollectionReference credentialsCollection;

    @Mock
    DocumentReference settingsDocRef;

    @Mock
    DocumentReference credentialsDocRef;

    @Mock
    DocumentSnapshot settingsDoc;

    @Mock
    DocumentSnapshot credentialsDoc;

    @Mock
    QuerySnapshot querySnapshot;

    @Mock
    QueryDocumentSnapshot queryDocumentSnapshot;

    @Mock
    Query query;

    @Mock
    Transaction transaction;

    FirestoreSettingRepositoryImpl target;

    @BeforeEach
    void setUp() {
        target = new FirestoreSettingRepositoryImpl(firestore);
    }

    @Nested
    @DisplayName("saveメソッドのテスト")
    class Save {

        @Test
        @DisplayName("Google連携情報がある設定情報が正常に保存されること")
        void testSave_WithCredentials() throws Exception {
            UserSetting setting = UserSetting.builder()
                    .slackUserId("U123")
                    .userName("ユーザー")
                    .remindTime("18:00")
                    .active(true)
                    .email("test@example.com")
                    .googleCalendarId("cal-123")
                    .googleRefreshToken("rt-123")
                    .createdAt(Instant.ofEpochSecond(1000L))
                    .updatedAt(Instant.ofEpochSecond(2000L))
                    .build();

            when(firestore.collection(FirestoreCollectionNames.USER_SETTINGS)).thenReturn(settingsCollection);
            when(settingsCollection.document("U123")).thenReturn(settingsDocRef);
            when(settingsDocRef.set(anyMap())).thenReturn(ApiFutures.immediateFuture(mock(WriteResult.class)));

            when(firestore.collection(FirestoreCollectionNames.USER_CREDENTIALS)).thenReturn(credentialsCollection);
            when(credentialsCollection.document("U123")).thenReturn(credentialsDocRef);
            when(credentialsDocRef.set(anyMap())).thenReturn(ApiFutures.immediateFuture(mock(WriteResult.class)));

            target.save(setting);

            verify(settingsDocRef).set(anyMap());
            verify(credentialsDocRef).set(anyMap());
        }

        @Test
        @DisplayName("Google連携情報がない設定情報が正常に保存されること")
        void testSave_WithoutCredentials() throws Exception {
            UserSetting setting = UserSetting.builder()
                    .slackUserId("U123")
                    .userName("ユーザー")
                    .remindTime("18:00")
                    .active(true)
                    .createdAt(Instant.ofEpochSecond(1000L))
                    .updatedAt(Instant.ofEpochSecond(2000L))
                    .build();

            when(firestore.collection(FirestoreCollectionNames.USER_SETTINGS)).thenReturn(settingsCollection);
            when(settingsCollection.document("U123")).thenReturn(settingsDocRef);
            when(settingsDocRef.set(anyMap())).thenReturn(ApiFutures.immediateFuture(mock(WriteResult.class)));

            target.save(setting);

            verify(settingsDocRef).set(anyMap());
            verify(firestore, never()).collection(FirestoreCollectionNames.USER_CREDENTIALS);
        }
    }

    @Nested
    @DisplayName("findByIdメソッドのテスト")
    class FindById {

        @Test
        @DisplayName("設定情報と認証情報が紐づいて返ること")
        void testFindById_Exist() throws Exception {
            String slackUserId = "U123";
            when(firestore.collection(FirestoreCollectionNames.USER_SETTINGS)).thenReturn(settingsCollection);
            when(settingsCollection.document(slackUserId)).thenReturn(settingsDocRef);
            when(settingsDocRef.get()).thenReturn(ApiFutures.immediateFuture(settingsDoc));

            when(settingsDoc.exists()).thenReturn(true);
            when(settingsDoc.getString("slackUserId")).thenReturn(slackUserId);
            when(settingsDoc.getString("userName")).thenReturn("ユーザー");
            when(settingsDoc.getString("remindTime")).thenReturn("18:00");
            when(settingsDoc.getBoolean("isActive")).thenReturn(true);
            when(settingsDoc.getLong("createdAt")).thenReturn(1000L);
            when(settingsDoc.getLong("updatedAt")).thenReturn(2000L);

            when(firestore.collection(FirestoreCollectionNames.USER_CREDENTIALS)).thenReturn(credentialsCollection);
            when(credentialsCollection.document(slackUserId)).thenReturn(credentialsDocRef);
            when(credentialsDocRef.get()).thenReturn(ApiFutures.immediateFuture(credentialsDoc));

            when(credentialsDoc.exists()).thenReturn(true);
            when(credentialsDoc.getString("googleEmail")).thenReturn("test@example.com");
            when(credentialsDoc.getString("googleCalendarId")).thenReturn("cal-123");
            when(credentialsDoc.getString("encryptedRefreshToken")).thenReturn("rt-123");

            Optional<UserSetting> result = target.findById(slackUserId);

            assertThat(result).isPresent();
            UserSetting s = result.get();
            assertThat(s.getSlackUserId()).isEqualTo(slackUserId);
            assertThat(s.getEmail()).isEqualTo("test@example.com");
            assertThat(s.isGoogleLinked()).isTrue();
        }

        @Test
        @DisplayName("設定情報が存在しない場合にemptyが返ること")
        void testFindById_NotExist() throws Exception {
            String slackUserId = "U123";
            when(firestore.collection(FirestoreCollectionNames.USER_SETTINGS)).thenReturn(settingsCollection);
            when(settingsCollection.document(slackUserId)).thenReturn(settingsDocRef);
            when(settingsDocRef.get()).thenReturn(ApiFutures.immediateFuture(settingsDoc));
            when(settingsDoc.exists()).thenReturn(false);

            Optional<UserSetting> result = target.findById(slackUserId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllActiveメソッドのテスト")
    class FindAllActive {

        @Test
        @DisplayName("アクティブなユーザー設定がリストとして返ること")
        void testFindAllActive() throws Exception {
            when(firestore.collection(FirestoreCollectionNames.USER_SETTINGS)).thenReturn(settingsCollection);
            when(settingsCollection.whereEqualTo("isActive", true)).thenReturn(query);
            when(query.get()).thenReturn(ApiFutures.immediateFuture(querySnapshot));

            when(querySnapshot.getDocuments()).thenReturn(List.of(queryDocumentSnapshot));
            when(queryDocumentSnapshot.getId()).thenReturn("U123");
            when(queryDocumentSnapshot.getString("slackUserId")).thenReturn("U123");
            when(queryDocumentSnapshot.getBoolean("isActive")).thenReturn(true);

            when(firestore.collection(FirestoreCollectionNames.USER_CREDENTIALS)).thenReturn(credentialsCollection);
            when(credentialsCollection.document("U123")).thenReturn(credentialsDocRef);
            when(firestore.getAll(any(DocumentReference[].class)))
                    .thenReturn(ApiFutures.immediateFuture(List.of(credentialsDoc)));

            when(credentialsDoc.getId()).thenReturn("U123");
            when(credentialsDoc.exists()).thenReturn(true);
            when(credentialsDoc.getString("googleEmail")).thenReturn("test@example.com");

            List<UserSetting> result = target.findAllActive();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSlackUserId()).isEqualTo("U123");
            assertThat(result.get(0).getEmail()).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("findByEmailメソッドのテスト")
    class FindByEmail {

        @Test
        @DisplayName("メールアドレスに一致するユーザー設定情報が取得できること")
        void testFindByEmail_Exist() throws Exception {
            String email = "test@example.com";
            when(firestore.collection(FirestoreCollectionNames.USER_CREDENTIALS)).thenReturn(credentialsCollection);
            when(credentialsCollection.whereEqualTo("googleEmail", email)).thenReturn(query);
            when(query.limit(1)).thenReturn(query);
            when(query.get()).thenReturn(ApiFutures.immediateFuture(querySnapshot));

            when(querySnapshot.isEmpty()).thenReturn(false);
            when(querySnapshot.getDocuments()).thenReturn(List.of(queryDocumentSnapshot));
            when(queryDocumentSnapshot.getId()).thenReturn("U123");
            when(queryDocumentSnapshot.exists()).thenReturn(true);
            when(queryDocumentSnapshot.getString("googleEmail")).thenReturn(email);
            when(queryDocumentSnapshot.getString("googleCalendarId")).thenReturn("cal-123");
            when(queryDocumentSnapshot.getString("encryptedRefreshToken")).thenReturn("rt-123");

            when(firestore.collection(FirestoreCollectionNames.USER_SETTINGS)).thenReturn(settingsCollection);
            when(settingsCollection.document("U123")).thenReturn(settingsDocRef);
            when(settingsDocRef.get()).thenReturn(ApiFutures.immediateFuture(settingsDoc));

            when(settingsDoc.exists()).thenReturn(true);
            when(settingsDoc.getString("slackUserId")).thenReturn("U123");

            Optional<UserSetting> result = target.findByEmail(email);

            assertThat(result).isPresent();
            assertThat(result.get().getSlackUserId()).isEqualTo("U123");
        }
    }

    @Nested
    @DisplayName("deleteメソッドのテスト")
    class Delete {

        @Test
        @DisplayName("設定と認証情報双方が正常に削除されること")
        void testDelete() throws Exception {
            String slackUserId = "U123";
            when(firestore.collection(FirestoreCollectionNames.USER_SETTINGS)).thenReturn(settingsCollection);
            when(settingsCollection.document(slackUserId)).thenReturn(settingsDocRef);
            when(settingsDocRef.delete()).thenReturn(ApiFutures.immediateFuture(mock(WriteResult.class)));

            when(firestore.collection(FirestoreCollectionNames.USER_CREDENTIALS)).thenReturn(credentialsCollection);
            when(credentialsCollection.document(slackUserId)).thenReturn(credentialsDocRef);
            when(credentialsDocRef.delete()).thenReturn(ApiFutures.immediateFuture(mock(WriteResult.class)));

            target.delete(slackUserId);

            verify(settingsDocRef).delete();
            verify(credentialsDocRef).delete();
        }
    }

    @Nested
    @DisplayName("saveWithMigrationメソッドのテスト")
    class SaveWithMigration {

        @Test
        @DisplayName("トランザクション内で移行処理が正常に実行されること")
        void testSaveWithMigration() throws Exception {
            String oldSlackUserId = "U_OLD";
            UserSetting newSetting = UserSetting.builder()
                    .slackUserId("U_NEW")
                    .userName("移行ユーザー")
                    .email("test@example.com")
                    .googleCalendarId("cal-123")
                    .googleRefreshToken("rt-123")
                    .build();

            when(firestore.collection(FirestoreCollectionNames.USER_SETTINGS)).thenReturn(settingsCollection);
            when(settingsCollection.document(oldSlackUserId)).thenReturn(settingsDocRef);
            when(settingsCollection.document("U_NEW")).thenReturn(settingsDocRef);

            when(firestore.collection(FirestoreCollectionNames.USER_CREDENTIALS)).thenReturn(credentialsCollection);
            when(credentialsCollection.document(oldSlackUserId)).thenReturn(credentialsDocRef);
            when(credentialsCollection.document("U_NEW")).thenReturn(credentialsDocRef);

            when(firestore.runTransaction(any())).thenAnswer(invocation -> {
                Transaction.Function<?> function = invocation.getArgument(0);
                Object result = function.updateCallback(transaction);
                return ApiFutures.immediateFuture(result);
            });

            target.saveWithMigration(oldSlackUserId, newSetting);

            verify(transaction, times(2)).delete(any(DocumentReference.class));
            verify(transaction, times(2)).set(any(DocumentReference.class), anyMap());
        }
    }
}
