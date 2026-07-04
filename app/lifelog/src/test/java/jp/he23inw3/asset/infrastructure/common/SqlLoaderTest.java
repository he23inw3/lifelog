package jp.he23inw3.asset.infrastructure.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SqlLoaderTest {

    @Test
    @DisplayName("存在するSQLファイルが正常にロードできること")
    void testLoad_Success() {
        String sql = SqlLoader.load("sql/bigquery/find_daily_log.sql");
        assertThat(sql).isNotEmpty();
        assertThat(sql).contains("SELECT");
    }

    @Test
    @DisplayName("存在しないSQLファイルパスを指定した場合、IllegalArgumentExceptionを投げること")
    void testLoad_NotFound() {
        assertThatThrownBy(() -> SqlLoader.load("sql/non_existent.sql"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SQL file not found at sql/non_existent.sql");
    }
}
