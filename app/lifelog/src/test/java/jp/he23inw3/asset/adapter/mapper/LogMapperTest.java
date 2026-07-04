package jp.he23inw3.asset.adapter.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jp.he23inw3.asset.adapter.dto.LogDetailResponse;
import jp.he23inw3.asset.adapter.dto.LogListResponse;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.model.Sentiment;
import jp.he23inw3.asset.domain.repository.dto.DailyLogSearchQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class LogMapperTest {

    private final LogMapper mapper = Mappers.getMapper(LogMapper.class);

    @Nested
    @DisplayName("LogからLogDetailResponseへの変換")
    class ToDetailResponse {

        @Test
        @DisplayName("全フィールドが正しくマッピングされること")
        void toDetailResponse_ShouldMapAllFields() {
            // Arrange
            Instant now = Instant.parse("2026-06-30T10:00:00Z");
            Log log = Log.builder()
                    .slackUserId("U123")
                    .logDate(LocalDate.of(2026, 6, 30))
                    .rawText("作業テキスト")
                    .workHours(7.5)
                    .tasks("開発作業")
                    .diary("楽しかった")
                    .sentiment(Sentiment.HAPPY)
                    .holiday(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // Act
            LogDetailResponse response = mapper.toDetailResponse(log);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getSlackUserId()).isEqualTo("U123");
            assertThat(response.getLogDate()).isEqualTo(LocalDate.of(2026, 6, 30));
            assertThat(response.getRawText()).isEqualTo("作業テキスト");
            assertThat(response.getWorkHours()).isEqualTo(7.5);
            assertThat(response.getTasks()).isEqualTo("開発作業");
            assertThat(response.getDiary()).isEqualTo("楽しかった");
            assertThat(response.getSentiment()).isEqualTo("HAPPY");
            assertThat(response.isHoliday()).isFalse();
            assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 30, 19, 0, 0)); // UTC 10:00 -> JST 19:00
            assertThat(response.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 30, 19, 0, 0));
        }

        @Test
        @DisplayName("nullを渡した場合はnullが返ること")
        void toDetailResponse_WithNull_ShouldReturnNull() {
            assertThat(mapper.toDetailResponse(null)).isNull();
        }
    }

    @Nested
    @DisplayName("LogからLogListResponse.Logへの変換")
    class ToListResponseLog {

        @Test
        @DisplayName("一覧表示に必要なフィールドのみ正しくマッピングされること")
        void toListResponseLog_ShouldMapListFields() {
            Log log = Log.builder()
                    .slackUserId("U123")
                    .logDate(LocalDate.of(2026, 6, 30))
                    .rawText("作業テキスト")
                    .workHours(7.5)
                    .tasks("開発作業")
                    .diary("楽しかった")
                    .sentiment(Sentiment.HAPPY)
                    .holiday(false)
                    .build();

            LogListResponse.Log response = mapper.toListResponseLog(log);

            assertThat(response).isNotNull();
            assertThat(response.getLogDate()).isEqualTo(LocalDate.of(2026, 6, 30));
            assertThat(response.getWorkHours()).isEqualTo(7.5);
            assertThat(response.getTasks()).isEqualTo("開発作業");
            assertThat(response.getDiary()).isEqualTo("楽しかった");
            assertThat(response.getSentiment()).isEqualTo("HAPPY");
            assertThat(response.isHoliday()).isFalse();
        }

        @Test
        @DisplayName("nullを渡した場合はnullが返ること")
        void toListResponseLog_WithNull_ShouldReturnNull() {
            assertThat(mapper.toListResponseLog(null)).isNull();
        }
    }

    @Nested
    @DisplayName("LogリストからLogListResponse.Logリストへの変換")
    class ToListResponseLogList {

        @Test
        @DisplayName("リストが正しく変換されること")
        void toListResponseLogList_ShouldMapList() {
            Log log1 = Log.builder().logDate(LocalDate.of(2026, 6, 1)).build();
            Log log2 = Log.builder().logDate(LocalDate.of(2026, 6, 2)).build();

            List<LogListResponse.Log> result = mapper.toListResponseLogList(Arrays.asList(log1, log2));

            assertThat(result).isNotNull().hasSize(2);
            assertThat(result.get(0).getLogDate()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(result.get(1).getLogDate()).isEqualTo(LocalDate.of(2026, 6, 2));
        }

        @Test
        @DisplayName("nullを渡した場合はnullが返ること")
        void toListResponseLogList_WithNull_ShouldReturnNull() {
            assertThat(mapper.toListResponseLogList(null)).isNull();
        }

        @Test
        @DisplayName("空のリストを渡した場合は空のリストが返ること")
        void toListResponseLogList_WithEmptyList_ShouldReturnEmptyList() {
            assertThat(mapper.toListResponseLogList(Collections.emptyList())).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("検索パラメータからDailyLogSearchQueryへの変換")
    class ToDailySearchQuery {

        @Test
        @DisplayName("ユーザーIDと日付範囲が正しくマッピングされること")
        void toDailySearchQuery_ShouldMapFields() {
            LocalDate from = LocalDate.of(2026, 6, 1);
            LocalDate to = LocalDate.of(2026, 6, 30);

            DailyLogSearchQuery query = mapper.toDailySearchQuery("U123", from, to);

            assertThat(query).isNotNull();
            assertThat(query.getSlackUserId()).isEqualTo("U123");
            assertThat(query.getStart()).isEqualTo(from);
            assertThat(query.getEnd()).isEqualTo(to);
        }
    }

    @Nested
    @DisplayName("InstantからLocalDateTimeへのマッピング")
    class MapInstantToLocalDateTime {

        @Test
        @DisplayName("InstantがJSTのLocalDateTimeに正しく変換されること")
        void mapInstantToLocalDateTime_ShouldConvertCorrectly() {
            Instant instant = Instant.parse("2026-06-30T15:00:00Z"); // UTC 15:00 -> JST 24:00 (翌日 00:00)
            LocalDateTime localDateTime = mapper.mapInstantToLocalDateTime(instant);
            assertThat(localDateTime).isEqualTo(LocalDateTime.of(2026, 7, 1, 0, 0, 0));
        }

        @Test
        @DisplayName("nullの場合はnullが返ること")
        void mapInstantToLocalDateTime_WithNull_ShouldReturnNull() {
            assertThat(mapper.mapInstantToLocalDateTime(null)).isNull();
        }
    }
}
