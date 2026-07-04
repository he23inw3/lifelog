package jp.he23inw3.asset.adapter.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import jp.he23inw3.asset.adapter.dto.LogDetailResponse;
import jp.he23inw3.asset.adapter.dto.LogListResponse;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.repository.dto.DailyLogSearchQuery;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * {@link Log} ドメインモデル ↔ adapter 層 DTO の変換マッパー。
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface LogMapper {

    /**
     * ドメインモデルを詳細レスポンス DTO に変換します。
     *
     * @param log 日報ログドメインモデル
     * @return 変換後の {@link LogDetailResponse}
     */
    LogDetailResponse toDetailResponse(Log log);

    /**
     * ドメインモデルを一覧レスポンス DTO に変換します。
     *
     * @param log 日報ログドメインモデル
     * @return 変換後の {@link LogListResponse.Log}
     */
    LogListResponse.Log toListResponseLog(Log log);

    /**
     * ドメインモデルのリストを一覧レスポンス DTO のリストに変換します。
     *
     * @param logs 日報ログリスト
     * @return 変換後の {@link LogListResponse.Log} のリスト
     */
    List<LogListResponse.Log> toListResponseLogList(List<Log> logs);

    @Mapping(target = "start", source = "from")
    @Mapping(target = "end", source = "to")
    DailyLogSearchQuery toDailySearchQuery(String slackUserId, LocalDate from, LocalDate to);

    /**
     * Instant を Asia/Tokyo タイムゾーンの LocalDateTime に変換します。
     *
     * @param instant 変換対象の Instant
     * @return 変換後の LocalDateTime
     */
    default LocalDateTime mapInstantToLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, DateTimeUtil.TOKYO_ZONE);
    }
}
