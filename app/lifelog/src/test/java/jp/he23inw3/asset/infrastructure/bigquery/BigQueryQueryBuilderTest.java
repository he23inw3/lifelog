package jp.he23inw3.asset.infrastructure.bigquery;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryParameterValue;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BigQueryQueryBuilderTest {

    @Test
    @DisplayName("selectFromのみを指定した場合、単純なSELECTクエリが組み立てられること")
    void build_SelectOnly() {
        // Act
        QueryJobConfiguration config = BigQueryQueryBuilder.selectFrom("my_dataset", "my_table")
                .build();

        // Assert
        assertThat(config.getQuery()).isEqualTo("SELECT * FROM `my_dataset.my_table`");
        assertThat(config.getNamedParameters()).isEmpty();
    }

    @Test
    @DisplayName("単一のwhere条件を指定した場合、WHERE句が正しく追加されること")
    void build_WithSingleWhere() {
        // Act
        QueryJobConfiguration config = BigQueryQueryBuilder.selectFrom("my_dataset", "my_table")
                .where("status = 'ACTIVE'")
                .build();

        // Assert
        assertThat(config.getQuery()).isEqualTo("SELECT * FROM `my_dataset.my_table` WHERE status = 'ACTIVE'");
        assertThat(config.getNamedParameters()).isEmpty();
    }

    @Test
    @DisplayName("パラメータ付きのwhere条件を指定した場合、条件とパラメータが正しく設定されること")
    void build_WithWhereAndParameter() {
        // Arrange
        QueryParameterValue paramValue = QueryParameterValue.string("U12345");

        // Act
        QueryJobConfiguration config = BigQueryQueryBuilder.selectFrom("my_dataset", "my_table")
                .where("slackUserId = @userId", "userId", paramValue)
                .build();

        // Assert
        assertThat(config.getQuery()).isEqualTo("SELECT * FROM `my_dataset.my_table` WHERE slackUserId = @userId");
        
        Map<String, QueryParameterValue> params = config.getNamedParameters();
        assertThat(params).containsOnlyKeys("userId");
        assertThat(params.get("userId")).isEqualTo(paramValue);
    }

    @Test
    @DisplayName("複数のwhere条件を指定した場合、ANDで結合されること")
    void build_WithMultipleWheres() {
        // Arrange
        QueryParameterValue userIdParam = QueryParameterValue.string("U12345");
        QueryParameterValue activeParam = QueryParameterValue.bool(true);

        // Act
        QueryJobConfiguration config = BigQueryQueryBuilder.selectFrom("my_dataset", "my_table")
                .where("slackUserId = @userId", "userId", userIdParam)
                .where("isActive = @active", "active", activeParam)
                .where("deletedAt IS NULL")
                .build();

        // Assert
        assertThat(config.getQuery()).isEqualTo(
                "SELECT * FROM `my_dataset.my_table` WHERE slackUserId = @userId AND isActive = @active AND deletedAt IS NULL"
        );
        
        Map<String, QueryParameterValue> params = config.getNamedParameters();
        assertThat(params).containsOnlyKeys("userId", "active");
        assertThat(params.get("userId")).isEqualTo(userIdParam);
        assertThat(params.get("active")).isEqualTo(activeParam);
    }

    @Test
    @DisplayName("orderByを指定した場合、ORDER BY句が正しく追加されること")
    void build_WithOrderBy() {
        // Act
        QueryJobConfiguration config = BigQueryQueryBuilder.selectFrom("my_dataset", "my_table")
                .orderBy("createdAt DESC")
                .build();

        // Assert
        assertThat(config.getQuery()).isEqualTo("SELECT * FROM `my_dataset.my_table` ORDER BY createdAt DESC");
        assertThat(config.getNamedParameters()).isEmpty();
    }

    @Test
    @DisplayName("WHERE句とORDER BY句の両方を指定した場合、順番通りに組み立てられること")
    void build_WithWhereAndOrderBy() {
        // Arrange
        QueryParameterValue userIdParam = QueryParameterValue.string("U12345");

        // Act
        QueryJobConfiguration config = BigQueryQueryBuilder.selectFrom("my_dataset", "my_table")
                .where("slackUserId = @userId", "userId", userIdParam)
                .orderBy("createdAt DESC")
                .build();

        // Assert
        assertThat(config.getQuery()).isEqualTo(
                "SELECT * FROM `my_dataset.my_table` WHERE slackUserId = @userId ORDER BY createdAt DESC"
        );
        
        Map<String, QueryParameterValue> params = config.getNamedParameters();
        assertThat(params).containsOnlyKeys("userId");
        assertThat(params.get("userId")).isEqualTo(userIdParam);
    }
}
