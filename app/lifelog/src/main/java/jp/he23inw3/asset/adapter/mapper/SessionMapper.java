package jp.he23inw3.asset.adapter.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import jp.he23inw3.asset.adapter.dto.SessionListResponse;
import jp.he23inw3.asset.adapter.dto.SessionResetResponse;
import jp.he23inw3.asset.domain.model.Session;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * {@link Session} ドメインモデル ↔ adapter 層 DTO の変換マッパー。
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface SessionMapper {

    /**
     * ドメインモデルをレスポンス DTO に変換します。
     *
     * @param session 対話セッションドメインモデル
     * @return 変換後の {@link SessionListResponse.SessionResponse}
     */
    SessionListResponse.SessionResponse toResponse(Session session);

    /**
     * ドメインモデルのリストをレスポンス DTO のリストに変換します。
     *
     * @param sessions 対話セッションリスト
     * @return 変換後の {@link SessionListResponse.SessionResponse} のリスト
     */
    List<SessionListResponse.SessionResponse> toResponseList(List<Session> sessions);

    /**
     * Instant を Asia/Tokyo タイムゾーンの LocalDateTime に変換します。
     *
     * @param instant 変換対象の Instant
     * @return 変換後の LocalDateTime
     */
    default LocalDateTime mapInstantToLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, DateTimeUtil.TOKYO_ZONE);
    }

    /**
     * リセット成功時のレスポンス DTO を構築します。
     *
     * @param slackUserId 対話セッションをクリアした対象の Slack ユーザーID
     * @param message 成功メッセージ
     * @return {@link SessionResetResponse}
     */
    default SessionResetResponse toResetResponse(String slackUserId, String message) {
        return SessionResetResponse.builder()
                .slackUserId(slackUserId)
                .message(message)
                .build();
    }
}
