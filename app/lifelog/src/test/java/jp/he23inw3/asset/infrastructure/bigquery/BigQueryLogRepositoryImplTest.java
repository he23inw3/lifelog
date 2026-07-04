package jp.he23inw3.asset.infrastructure.bigquery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.exception.ExternalServiceException;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.model.Sentiment;
import jp.he23inw3.asset.domain.repository.dto.DailyLogSearchQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BigQueryLogRepositoryImplTest {

    @Mock
    BigQuery bigQuery;

    @Mock
    LifeLogConfig config;

    @Mock
    LifeLogConfig.Bigquery bqConfig;

    @Mock
    TableResult tableResult;

    @Mock
    FieldValueList fieldValueList;

    BigQueryLogRepositoryImpl target;

    @BeforeEach
    void setUp() {
        when(config.bigquery()).thenReturn(bqConfig);
        when(bqConfig.dataset()).thenReturn("test_dataset");
        target = new BigQueryLogRepositoryImpl(bigQuery, config);
    }

    private FieldValue mockFieldValue(String value) {
        FieldValue fv = mock(FieldValue.class);
        lenient().when(fv.getStringValue()).thenReturn(value);
        lenient().when(fv.isNull()).thenReturn(value == null);
        return fv;
    }

    private FieldValue mockFieldValue(Double value) {
        FieldValue fv = mock(FieldValue.class);
        lenient().when(fv.getDoubleValue()).thenReturn(value);
        lenient().when(fv.isNull()).thenReturn(value == null);
        return fv;
    }

    private FieldValue mockFieldValue(Boolean value) {
        FieldValue fv = mock(FieldValue.class);
        lenient().when(fv.getBooleanValue()).thenReturn(value);
        lenient().when(fv.isNull()).thenReturn(value == null);
        return fv;
    }

    private FieldValue mockFieldValue(Long value, boolean isTimestamp) {
        FieldValue fv = mock(FieldValue.class);
        if (isTimestamp) {
            lenient().when(fv.getTimestampValue()).thenReturn(value);
        } else {
            lenient().when(fv.getLongValue()).thenReturn(value);
        }
        lenient().when(fv.isNull()).thenReturn(value == null);
        return fv;
    }

    private void mockRow(FieldValueList row, Log log) {
        FieldValue slackUserId = mockFieldValue(log.getSlackUserId());
        FieldValue logDate = mockFieldValue(log.getLogDate().toString());
        FieldValue rawText = mockFieldValue(log.getRawText());
        FieldValue isHoliday = mockFieldValue(log.isHoliday());
        FieldValue tasks = mockFieldValue(log.getTasks());
        FieldValue workHours = mockFieldValue(log.getWorkHours());
        FieldValue overtimeHours = mockFieldValue(log.getOvertimeHours());
        FieldValue diary = mockFieldValue(log.getDiary());
        FieldValue sentiment = mockFieldValue(log.getSentiment() != null ? log.getSentiment().getValue() : null);
        FieldValue createdAt = mockFieldValue(log.getCreatedAt().getEpochSecond() * 1000000L, true);
        FieldValue updatedAt = mockFieldValue(log.getUpdatedAt().getEpochSecond() * 1000000L, true);

        when(row.get("slack_user_id")).thenReturn(slackUserId);
        when(row.get("log_date")).thenReturn(logDate);
        when(row.get("raw_text")).thenReturn(rawText);
        when(row.get("is_holiday")).thenReturn(isHoliday);
        when(row.get("tasks")).thenReturn(tasks);
        when(row.get("work_hours")).thenReturn(workHours);
        when(row.get("overtime_hours")).thenReturn(overtimeHours);
        when(row.get("diary")).thenReturn(diary);
        when(row.get("sentiment")).thenReturn(sentiment);
        when(row.get("created_at")).thenReturn(createdAt);
        when(row.get("updated_at")).thenReturn(updatedAt);
    }

    @Nested
    @DisplayName("saveメソッドのテスト")
    class Save {

        @Test
        @DisplayName("日報が正常にBigQueryへクエリ実行され保存されること")
        void testSave_Success() throws Exception {
            Log entry = Log.builder()
                    .slackUserId("U123")
                    .logDate(LocalDate.of(2026, 6, 30))
                    .rawText("開発業務")
                    .holiday(false)
                    .tasks("開発")
                    .workHours(7.5)
                    .overtimeHours(1.0)
                    .diary("捗った")
                    .sentiment(Sentiment.HAPPY)
                    .createdAt(Instant.ofEpochSecond(1000L))
                    .updatedAt(Instant.ofEpochSecond(2000L))
                    .build();

            when(bigQuery.query(any(QueryJobConfiguration.class))).thenReturn(tableResult);
            when(tableResult.iterateAll()).thenReturn(List.of());

            target.save(entry);

            verify(bigQuery, times(2)).query(any(QueryJobConfiguration.class));
        }

        @Test
        @DisplayName("BigQuery例外が発生した際にラップされた例外が投げられること")
        void testSave_ThrowsException() throws Exception {
            Log entry = Log.builder()
                    .slackUserId("U123")
                    .logDate(LocalDate.of(2026, 6, 30))
                    .rawText("開発業務")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            when(bigQuery.query(any(QueryJobConfiguration.class))).thenThrow(new BigQueryException(500, "BigQuery error"));

            assertThatThrownBy(() -> target.save(entry))
                    .isInstanceOf(ExternalServiceException.class)
                    .hasMessageContaining("BigQuery のクエリ実行に失敗しました。");
        }
    }

    @Nested
    @DisplayName("findByUserIdAndPeriodメソッドのテスト")
    class FindByUserIdAndPeriod {

        @Test
        @DisplayName("期間とユーザーIDに対応するログ一覧が返ること")
        void testFindByUserIdAndPeriod_Success() throws Exception {
            DailyLogSearchQuery query = DailyLogSearchQuery.builder()
                    .slackUserId("U123")
                    .start(LocalDate.of(2026, 6, 1))
                    .end(LocalDate.of(2026, 6, 30))
                    .build();

            Log logModel = Log.builder()
                    .slackUserId("U123")
                    .logDate(LocalDate.of(2026, 6, 10))
                    .rawText("テキスト")
                    .holiday(false)
                    .tasks("tasks")
                    .workHours(8.0)
                    .overtimeHours(0.0)
                    .diary("diary")
                    .sentiment(Sentiment.HAPPY)
                    .createdAt(Instant.ofEpochSecond(1000L))
                    .updatedAt(Instant.ofEpochSecond(2000L))
                    .build();

            when(bigQuery.query(any(QueryJobConfiguration.class))).thenReturn(tableResult);
            when(tableResult.iterateAll()).thenReturn(List.of(fieldValueList));
            mockRow(fieldValueList, logModel);

            List<Log> result = target.findByUserIdAndPeriod(query);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSlackUserId()).isEqualTo("U123");
            assertThat(result.get(0).getTasks()).isEqualTo("tasks");
        }
    }

    @Nested
    @DisplayName("findByUserIdAndDateメソッドのテスト")
    class FindByUserIdAndDate {

        @Test
        @DisplayName("特定の日付のログが存在する場合、ログが返ること")
        void testFindByUserIdAndDate_Exist() throws Exception {
            String userId = "U123";
            LocalDate date = LocalDate.of(2026, 6, 30);

            Log logModel = Log.builder()
                    .slackUserId(userId)
                    .logDate(date)
                    .rawText("text")
                    .holiday(false)
                    .tasks("tasks")
                    .workHours(7.5)
                    .overtimeHours(0.0)
                    .diary("diary")
                    .sentiment(Sentiment.NEUTRAL)
                    .createdAt(Instant.ofEpochSecond(1000L))
                    .updatedAt(Instant.ofEpochSecond(2000L))
                    .build();

            when(bigQuery.query(any(QueryJobConfiguration.class))).thenReturn(tableResult);
            when(tableResult.iterateAll()).thenReturn(List.of(fieldValueList));
            mockRow(fieldValueList, logModel);

            Optional<Log> result = target.findByUserIdAndDate(userId, date);

            assertThat(result).isPresent();
            assertThat(result.get().getSlackUserId()).isEqualTo(userId);
            assertThat(result.get().getTasks()).isEqualTo("tasks");
        }

        @Test
        @DisplayName("特定の日付のログが存在しない場合、emptyが返ること")
        void testFindByUserIdAndDate_NotExist() throws Exception {
            when(bigQuery.query(any(QueryJobConfiguration.class))).thenReturn(tableResult);
            when(tableResult.iterateAll()).thenReturn(List.of());

            Optional<Log> result = target.findByUserIdAndDate("U123", LocalDate.of(2026, 6, 30));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByDateメソッドのテスト")
    class CountByDate {

        @Test
        @DisplayName("指定日付のログ件数が正しく取得されること")
        void testCountByDate() throws Exception {
            LocalDate date = LocalDate.of(2026, 6, 30);

            when(bigQuery.query(any(QueryJobConfiguration.class))).thenReturn(tableResult);
            when(tableResult.iterateAll()).thenReturn(List.of(fieldValueList));

            FieldValue cntVal = mock(FieldValue.class);
            when(fieldValueList.get("cnt")).thenReturn(cntVal);
            when(cntVal.getLongValue()).thenReturn(5L);

            long count = target.countByDate(date);

            assertThat(count).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("findByAdminQueryメソッドのテスト")
    class FindByAdminQuery {

        @Test
        @DisplayName("管理者用クエリでの検索結果が返ること")
        void testFindByAdminQuery() throws Exception {
            Log logModel = Log.builder()
                    .slackUserId("U123")
                    .logDate(LocalDate.of(2026, 6, 30))
                    .rawText("text")
                    .holiday(false)
                    .tasks("tasks")
                    .workHours(7.5)
                    .overtimeHours(0.0)
                    .diary("diary")
                    .sentiment(Sentiment.NEUTRAL)
                    .createdAt(Instant.ofEpochSecond(1000L))
                    .updatedAt(Instant.ofEpochSecond(2000L))
                    .build();

            when(bigQuery.query(any(QueryJobConfiguration.class))).thenReturn(tableResult);
            when(tableResult.iterateAll()).thenReturn(List.of(fieldValueList));
            mockRow(fieldValueList, logModel);

            List<Log> result = target.findByAdminQuery("U123", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), false, Sentiment.NEUTRAL);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSlackUserId()).isEqualTo("U123");
        }
    }
}
