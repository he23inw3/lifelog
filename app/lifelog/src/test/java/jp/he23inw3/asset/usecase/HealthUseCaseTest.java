package jp.he23inw3.asset.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.enterprise.inject.Instance;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jp.he23inw3.asset.domain.model.HealthStatus;
import jp.he23inw3.asset.domain.repository.ExternalHealthRepository;
import jp.he23inw3.asset.domain.model.HealthCheckResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HealthUseCaseTest {

    @Mock
    Instance<ExternalHealthRepository> healthRepositories;

    @InjectMocks
    HealthUseCase target;

    @Nested
    @DisplayName("ヘルスチェック実行")
    class CheckHealth {

        @Test
        @DisplayName("外部接続が全て正常の場合、HealthStatus.UPを返すこと")
        void check_AllUp_ShouldReturnUp() {
            // Arrange
            ExternalHealthRepository repo1 = mock(ExternalHealthRepository.class);
            when(repo1.getServiceName()).thenReturn("Firestore");
            when(repo1.checkHealth()).thenReturn(HealthStatus.UP);

            ExternalHealthRepository repo2 = mock(ExternalHealthRepository.class);
            when(repo2.getServiceName()).thenReturn("BigQuery");
            when(repo2.checkHealth()).thenReturn(HealthStatus.UP);

            List<ExternalHealthRepository> list = Arrays.asList(repo1, repo2);
            when(healthRepositories.iterator()).thenReturn(list.iterator());

            // Act
            HealthCheckResult actual = target.check();

            // Assert
            assertThat(actual.getStatus()).isEqualTo(HealthStatus.UP);
            assertThat(actual.getComponents()).containsOnly(entry("Firestore", HealthStatus.UP),
                    entry("BigQuery", HealthStatus.UP));
        }

        @Test
        @DisplayName("外部接続が一つでも異常の場合、HealthStatus.DOWNを返すこと")
        void check_OneDown_ShouldReturnDown() {
            // Arrange
            ExternalHealthRepository repo1 = mock(ExternalHealthRepository.class);
            when(repo1.getServiceName()).thenReturn("Firestore");
            when(repo1.checkHealth()).thenReturn(HealthStatus.UP);

            ExternalHealthRepository repo2 = mock(ExternalHealthRepository.class);
            when(repo2.getServiceName()).thenReturn("BigQuery");
            when(repo2.checkHealth()).thenReturn(HealthStatus.DOWN);

            List<ExternalHealthRepository> list = Arrays.asList(repo1, repo2);
            when(healthRepositories.iterator()).thenReturn(list.iterator());

            // Act
            HealthCheckResult actual = target.check();

            // Assert
            assertThat(actual.getStatus()).isEqualTo(HealthStatus.DOWN);
            assertThat(actual.getComponents()).containsOnly(entry("Firestore", HealthStatus.UP),
                    entry("BigQuery", HealthStatus.DOWN));
        }

        @Test
        @DisplayName("外部接続のヘルスチェック結果がnullの場合、HealthStatus.DOWNとみなすこと")
        void check_StatusNull_ShouldFallbackToDown() {
            // Arrange
            ExternalHealthRepository repo1 = mock(ExternalHealthRepository.class);
            when(repo1.getServiceName()).thenReturn("Firestore");
            when(repo1.checkHealth()).thenReturn(null);

            List<ExternalHealthRepository> list = Collections.singletonList(repo1);
            when(healthRepositories.iterator()).thenReturn(list.iterator());

            // Act
            HealthCheckResult actual = target.check();

            // Assert
            assertThat(actual.getStatus()).isEqualTo(HealthStatus.DOWN);
            assertThat(actual.getComponents()).containsOnly(entry("Firestore", HealthStatus.DOWN));
        }

        @Test
        @DisplayName("外部接続がない場合、HealthStatus.UPを返すこと")
        void check_EmptyRepositories_ShouldReturnUp() {
            // Arrange
            List<ExternalHealthRepository> list = Collections.emptyList();
            when(healthRepositories.iterator()).thenReturn(list.iterator());

            // Act
            HealthCheckResult actual = target.check();

            // Assert
            assertThat(actual.getStatus()).isEqualTo(HealthStatus.UP);
            assertThat(actual.getComponents()).isEmpty();
        }
    }
}
