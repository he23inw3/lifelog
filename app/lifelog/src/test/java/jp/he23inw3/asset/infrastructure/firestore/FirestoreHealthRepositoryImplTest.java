package jp.he23inw3.asset.infrastructure.firestore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import jp.he23inw3.asset.domain.model.HealthStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FirestoreHealthRepositoryImplTest {

    @Mock
    Firestore firestore;

    @InjectMocks
    FirestoreHealthRepositoryImpl target;

    @Nested
    @DisplayName("ヘルスチェック")
    class CheckHealth {

        @Test
        @DisplayName("Firestoreの疎通チェック正常時、UPを返すこと")
        @SuppressWarnings("unchecked")
        void checkHealth_Success_ShouldReturnUp() throws Exception {
            // Arrange
            CollectionReference collectionRef = mock(CollectionReference.class);
            DocumentReference docRef = mock(DocumentReference.class);
            ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
            DocumentSnapshot snapshot = mock(DocumentSnapshot.class);

            when(firestore.collection("health_check")).thenReturn(collectionRef);
            when(collectionRef.document("status")).thenReturn(docRef);
            when(docRef.get()).thenReturn(future);
            when(future.get()).thenReturn(snapshot);

            // Act
            HealthStatus actual = target.checkHealth();

            // Assert
            assertThat(actual).isEqualTo(HealthStatus.UP);
            assertThat(target.getServiceName()).isEqualTo("Firestore");
        }

        @Test
        @DisplayName("Firestoreの疎通チェック異常時、DOWNを返すこと")
        @SuppressWarnings("unchecked")
        void checkHealth_Failure_ShouldReturnDown() {
            // Arrange
            when(firestore.collection(anyString())).thenThrow(new RuntimeException("Connection timed out"));

            // Act
            HealthStatus actual = target.checkHealth();

            // Assert
            assertThat(actual).isEqualTo(HealthStatus.DOWN);
        }
    }
}
