package jp.he23inw3.asset.infrastructure.bigquery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.gax.paging.Page;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.Dataset;
import jp.he23inw3.asset.domain.model.HealthStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BigQueryHealthRepositoryImplTest {

    @Mock
    BigQuery bigQuery;

    @InjectMocks
    BigQueryHealthRepositoryImpl target;

    @Nested
    @DisplayName("ヘルスチェック")
    class CheckHealth {

        @Test
        @DisplayName("BigQueryの疎通チェック正常時、UPを返すこと")
        void checkHealth_Success_ShouldReturnUp() {
            // Arrange
            Page<Dataset> mockPage = mock(Page.class);
            when(bigQuery.listDatasets(BigQuery.DatasetListOption.pageSize(1))).thenReturn(mockPage);

            // Act
            HealthStatus actual = target.checkHealth();

            // Assert
            assertThat(actual).isEqualTo(HealthStatus.UP);
            assertThat(target.getServiceName()).isEqualTo("BigQuery");
            verify(bigQuery).listDatasets(BigQuery.DatasetListOption.pageSize(1));
        }

        @Test
        @DisplayName("BigQueryの疎通チェック異常時、DOWNを返すこと")
        void checkHealth_Failure_ShouldReturnDown() {
            // Arrange
            when(bigQuery.listDatasets(BigQuery.DatasetListOption.pageSize(1))).thenThrow(new RuntimeException("BigQuery query error"));

            // Act
            HealthStatus actual = target.checkHealth();

            // Assert
            assertThat(actual).isEqualTo(HealthStatus.DOWN);
        }
    }
}
