package jp.he23inw3.asset.infrastructure.bigquery;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryParameterValue;
import com.google.cloud.bigquery.TableResult;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.constant.BigQueryTableNames;
import jp.he23inw3.asset.domain.exception.ExternalServiceException;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.model.Sentiment;
import jp.he23inw3.asset.domain.repository.DailyLogRepository;
import jp.he23inw3.asset.domain.repository.dto.DailyLogSearchQuery;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import jp.he23inw3.asset.domain.util.InstantUtil;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import jp.he23inw3.asset.infrastructure.common.SqlLoader;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * BigQuery の {@code daily_logs} テーブルに対するリポジトリ実装クラス。
 * <p>
 * 外出しした SQL テンプレートファイルを {@link SqlLoader} を通じて読み込み、 プレースホルダ（データセット・テーブル名）の置換および名前付きパラメータの設定を行います。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class BigQueryLogRepositoryImpl implements DailyLogRepository {

    private final BigQuery bigQuery;

    private final LifeLogConfig config;

    /**
     * 日報ログを BigQuery に冪等に保存（新規挿入または更新）します。
     *
     * @param entry 保存対象の日報ログドメインモデル
     * @throws ExternalServiceException BigQuery のクエリ実行に失敗した場合
     */
    @Override
    public void save(Log entry) {
        String dataset = config.bigquery().dataset();
        String table = BigQueryTableNames.DAILY_LOGS;
        String now = DateTimeUtil.toBigQueryTimestampString(InstantUtil.now());

        Instant createdAt = entry.getCreatedAt();
        try {
            Optional<Log> existing = findByUserIdAndDate(entry.getSlackUserId(), entry.getLogDate());
            if (existing.isPresent()) {
                createdAt = existing.get().getCreatedAt();
            }
        } catch (Exception e) {
            log.warn(MessageHelper.getMessage("infra.bigquery.save.check.error", e.getMessage()));
        }

        if (createdAt == null) {
            createdAt = InstantUtil.now();
        }
        String createdAtStr = DateTimeUtil.toBigQueryTimestampString(createdAt);

        String sqlTemplate = SqlLoader.load("sql/bigquery/upsert_daily_log.sql");
        String query = sqlTemplate.replace("{dataset}", dataset).replace("{table}", table);

        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query)
                .addNamedParameter("slackUserId", QueryParameterValue.string(entry.getSlackUserId()))
                .addNamedParameter("logDate", QueryParameterValue.string(entry.getLogDate().toString()))
                .addNamedParameter("rawText", QueryParameterValue.string(entry.getRawText()))
                .addNamedParameter("isHoliday", QueryParameterValue.bool(entry.isHoliday()))
                .addNamedParameter("tasks", QueryParameterValue.string(StringUtils.defaultString(entry.getTasks())))
                .addNamedParameter("workHours",
                        QueryParameterValue.float64(entry.getWorkHours() != null ? entry.getWorkHours() : 0.0))
                .addNamedParameter("overtimeHours",
                        QueryParameterValue.float64(entry.getOvertimeHours() != null ? entry.getOvertimeHours() : 0.0))
                .addNamedParameter("diary", QueryParameterValue.string(StringUtils.defaultString(entry.getDiary())))
                .addNamedParameter("sentiment", QueryParameterValue.string(
                        Sentiment.getValueOrDefault(entry.getSentiment())))
                .addNamedParameter("createdAt", QueryParameterValue.string(createdAtStr))
                .addNamedParameter("updatedAt", QueryParameterValue.string(now))
                .addNamedParameter("traceId", QueryParameterValue.string(TraceHelper.currentTraceId())).build();

        try {
            bigQuery.query(queryConfig);
            log.info(MessageHelper.getMessage("infra.bigquery.save", entry.getSlackUserId(), entry.getLogDate()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("BigQuery のクエリ実行が中断されました。", e);
        } catch (BigQueryException e) {
            throw new ExternalServiceException("BigQuery のクエリ実行に失敗しました。", e);
        }
    }

    /**
     * 指定されたユーザーIDと期間（開始日〜終了日）に該当する日報ログの一覧を取得します。
     *
     * @param slackUserId Slack ユーザーID
     * @param start 開始日
     * @param end 終了日
     * @return 該当する日報ログのリスト（日付の昇順）
     * @throws ExternalServiceException BigQuery のクエリ実行に失敗した場合
     */
    @Override
    public List<Log> findByUserIdAndPeriod(DailyLogSearchQuery query) {
        String dataset = config.bigquery().dataset();
        String table = BigQueryTableNames.DAILY_LOGS;

        String sqlTemplate = SqlLoader.load("sql/bigquery/find_daily_logs_by_month.sql");
        String queryStr = sqlTemplate.replace("{dataset}", dataset).replace("{table}", table);

        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(queryStr)
                .addNamedParameter("slackUserId", QueryParameterValue.string(query.getSlackUserId()))
                .addNamedParameter("firstDay", QueryParameterValue.string(query.getStart().toString()))
                .addNamedParameter("lastDay", QueryParameterValue.string(query.getEnd().toString())).build();

        try {
            TableResult result = bigQuery.query(queryConfig);
            List<Log> logs = new ArrayList<>();
            for (FieldValueList row : result.iterateAll()) {
                logs.add(mapRowToLog(row));
            }
            return logs;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("BigQuery のクエリ実行が中断されました。", e);
        } catch (BigQueryException e) {
            throw new ExternalServiceException("BigQuery のクエリ実行に失敗しました。", e);
        }
    }

    /**
     * 指定されたユーザーIDと対象日に該当する日報ログを1件取得します。
     *
     * @param slackUserId Slack ユーザーID
     * @param date 対象日
     * @return 該当する日報ログ（存在しない場合は空の {@link Optional}）
     * @throws ExternalServiceException BigQuery のクエリ実行に失敗した場合
     */
    @Override
    public Optional<Log> findByUserIdAndDate(String slackUserId, LocalDate date) {
        String dataset = config.bigquery().dataset();
        String table = BigQueryTableNames.DAILY_LOGS;

        String sqlTemplate = SqlLoader.load("sql/bigquery/find_daily_log.sql");
        String query = sqlTemplate.replace("{dataset}", dataset).replace("{table}", table);

        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query)
                .addNamedParameter("slackUserId", QueryParameterValue.string(slackUserId))
                .addNamedParameter("logDate", QueryParameterValue.string(date.toString())).build();

        try {
            TableResult result = bigQuery.query(queryConfig);
            for (FieldValueList row : result.iterateAll()) {
                return Optional.of(mapRowToLog(row));
            }
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("BigQuery のクエリ実行が中断されました。", e);
        } catch (BigQueryException e) {
            throw new ExternalServiceException("BigQuery のクエリ実行に失敗しました。", e);
        }
    }

    /**
     * 指定された日付に登録された日報ログの総件数を取得します。
     *
     * @param date 対象の日付
     * @return 該当する日報ログの件数
     */
    @Override
    public long countByDate(LocalDate date) {
        String dataset = config.bigquery().dataset();
        String table = BigQueryTableNames.DAILY_LOGS;

        String sqlTemplate = SqlLoader.load("sql/bigquery/count_daily_logs_by_date.sql");
        String query = sqlTemplate.replace("{dataset}", dataset).replace("{table}", table);

        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query)
                .addNamedParameter("logDate", QueryParameterValue.string(date.toString())).build();

        try {
            TableResult result = bigQuery.query(queryConfig);
            for (FieldValueList row : result.iterateAll()) {
                return row.get("cnt").getLongValue();
            }
            return 0L;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("BigQuery のクエリ実行が中断されました。", e);
        } catch (BigQueryException e) {
            throw new ExternalServiceException("BigQuery のクエリ実行に失敗しました。", e);
        }
    }

    /**
     * 管理用条件に基づいて日報ログ一覧を検索します。
     *
     * @param user Slack ユーザーID（部分一致、空値の場合は全件）
     * @param from 検索開始日
     * @param to 検索終了日
     * @param holiday 休暇フラグ
     * @param sentiment 感情
     * @return 条件に合致する日報ログのリスト
     */
    @Override
    public List<Log> findByAdminQuery(String user, LocalDate from, LocalDate to, Boolean holiday, Sentiment sentiment) {
        String dataset = config.bigquery().dataset();
        String table = BigQueryTableNames.DAILY_LOGS;

        BigQueryQueryBuilder builder = BigQueryQueryBuilder.selectFrom(dataset, table);
        if (StringUtils.isNotEmpty(user)) {
            builder.where("slack_user_id = @slackUserId", "slackUserId", QueryParameterValue.string(user));
        }
        if (from != null) {
            builder.where("log_date >= DATE(@fromDate)", "fromDate", QueryParameterValue.string(from.toString()));
        }
        if (to != null) {
            builder.where("log_date <= DATE(@toDate)", "toDate", QueryParameterValue.string(to.toString()));
        }
        if (holiday != null) {
            builder.where("is_holiday = @isHoliday", "isHoliday", QueryParameterValue.bool(holiday));
        }
        if (sentiment != null) {
            builder.where("sentiment = @sentiment", "sentiment", QueryParameterValue.string(sentiment.getValue()));
        }
        builder.orderBy("log_date DESC, slack_user_id ASC");

        try {
            TableResult result = bigQuery.query(builder.build());
            List<Log> logs = new ArrayList<>();
            for (FieldValueList row : result.iterateAll()) {
                logs.add(mapRowToLog(row));
            }
            return logs;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("BigQuery のクエリ実行が中断されました。", e);
        } catch (BigQueryException e) {
            throw new ExternalServiceException("BigQuery のクエリ実行に失敗しました。", e);
        }
    }

    /**
     * BigQuery の行レコードから Log ドメインモデルへのマッピングを行います。
     *
     * @param row BigQuery の単一行レコード
     * @return マッピング後の日報ログドメインモデル
     */
    private Log mapRowToLog(FieldValueList row) {
        return Log.builder()
                .slackUserId(row.get("slack_user_id").getStringValue())
                .logDate(LocalDate.parse(row.get("log_date").getStringValue()))
                .rawText(row.get("raw_text").getStringValue())
                .holiday(row.get("is_holiday").getBooleanValue())
                .tasks(row.get("tasks").isNull() ? null : row.get("tasks").getStringValue())
                .workHours(row.get("work_hours").isNull() ? null : row.get("work_hours").getDoubleValue())
                .overtimeHours(row.get("overtime_hours").isNull() ? null : row.get("overtime_hours").getDoubleValue())
                .diary(row.get("diary").isNull() ? null : row.get("diary").getStringValue())
                .sentiment(row.get("sentiment").isNull()
                        ? null
                        : Sentiment.fromValue(row.get("sentiment").getStringValue()))
                .createdAt(InstantUtil.ofEpochMicro(row.get("created_at").getTimestampValue()))
                .updatedAt(InstantUtil.ofEpochMicro(row.get("updated_at").getTimestampValue())).build();
    }
}
