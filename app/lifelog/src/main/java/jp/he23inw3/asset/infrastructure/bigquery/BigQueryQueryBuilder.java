package jp.he23inw3.asset.infrastructure.bigquery;

import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryParameterValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * BigQuery 向けの動的 SQL とパラメータバインドを管理するヘルパークラス。
 */
public class BigQueryQueryBuilder {

    private final List<String> conditions = new ArrayList<>();

    private final Map<String, QueryParameterValue> parameters = new HashMap<>();

    private final String selectClause;

    private String orderByClause = "";

    /**
     * SELECT ... FROM ... 形式のクエリビルダを生成します。
     * 
     * @param dataset BigQueryデータセット名
     * @param table BigQueryテーブル名
     * @return BigQueryQueryBuilder
     */
    public static BigQueryQueryBuilder selectFrom(String dataset, String table) {
        return new BigQueryQueryBuilder(dataset, table);
    }

    /**
     * クエリをビルドします。
     * 
     * @return QueryJobConfiguration
     */
    public QueryJobConfiguration build() {
        StringBuilder sql = new StringBuilder(selectClause);

        if (CollectionUtils.isNotEmpty(conditions)) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        if (StringUtils.isNotBlank(orderByClause)) {
            sql.append(" ORDER BY ").append(orderByClause);
        }

        QueryJobConfiguration.Builder builder = QueryJobConfiguration.newBuilder(sql.toString());
        parameters.forEach(builder::addNamedParameter);
        return builder.build();
    }

    /**
     * ORDER BY句を追加します。
     * 
     * @param orderBy ORDER BY句
     * @return BigQueryQueryBuilder
     */
    public BigQueryQueryBuilder orderBy(String orderBy) {
        this.orderByClause = orderBy;
        return this;
    }

    /**
     * WHERE句を追加します。
     * 
     * @param sqlCondition SQLの条件式
     * @return BigQueryQueryBuilder
     */
    public BigQueryQueryBuilder where(String sqlCondition) {
        conditions.add(sqlCondition);
        return this;
    }

    /**
     * WHERE句を追加します。
     * 
     * @param sqlCondition SQLの条件式
     * @param paramName パラメータ名
     * @param paramValue パラメータ値
     * @return BigQueryQueryBuilder
     */
    public BigQueryQueryBuilder where(String sqlCondition, String paramName, QueryParameterValue paramValue) {
        conditions.add(sqlCondition);
        parameters.put(paramName, paramValue);
        return this;
    }

    private BigQueryQueryBuilder(String dataset, String table) {
        this.selectClause = "SELECT * FROM `" + dataset + "." + table + "`";
    }
}
