package jp.he23inw3.asset.adapter.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import jp.he23inw3.asset.adapter.constant.ApiPath;
import jp.he23inw3.asset.adapter.constant.ApiTag;
import jp.he23inw3.asset.adapter.dto.SessionListResponse;
import jp.he23inw3.asset.adapter.dto.SessionResetResponse;
import jp.he23inw3.asset.adapter.mapper.SessionMapper;
import jp.he23inw3.asset.domain.constant.UserMessageConstants;
import jp.he23inw3.asset.domain.model.Session;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import jp.he23inw3.asset.usecase.SessionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * 管理者向けセッション管理エンドポイントを提供するREST リソースクラス
 * <p>
 * OIDC 認証が必須であり、管理者のみ操作可能です セッション一覧の取得および任意ユーザーのセッション強制リセットを受け付けます
 */
@Slf4j
@Path(ApiPath.ADMIN_SESSIONS)
@Tag(name = ApiTag.ADMIN)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class SessionResource {

    private final SessionUseCase sessionUseCase;

    private final SessionMapper sessionMapper;

    /**
     * 現在進行中のすべてのセッション一覧を取得します
     * 
     * @return 進行中のセッション一覧レスポンス DTO
     */
    @GET
    @Operation(operationId = "BE-API409", summary = "セッション一覧", description = "現在進行中のすべてのセッション一覧を取得します")
    public SessionListResponse getSessions() {
        List<Session> sessions = sessionUseCase.getAllSessions();
        List<SessionListResponse.SessionResponse> responseList = sessionMapper.toResponseList(sessions);
        return SessionListResponse.builder()
                .totalSize(CollectionUtils.size(responseList))
                .sessions(responseList)
                .build();
    }

    /**
     * 指定された Slack ユーザーの対話セッションを強制的にリセット（破棄）する
     * 
     * @param slackUserId 対話セッションをクリアする対象の Slack ユーザーID
     * @return HTTP 200 OK とリセット結果の DTO
     */
    @DELETE
    @Path("/{slackUserId}")
    @Operation(operationId = "BE-API201", summary = "セッションリセット", description = "指定されたユーザーの対話セッションを強制リセットします")
    public SessionResetResponse resetSession(@PathParam("slackUserId") String slackUserId) {
        log.info(MessageHelper.getMessage("adapter.rest.session.reset", slackUserId));

        sessionUseCase.resetSession(slackUserId);

        return sessionMapper.toResetResponse(slackUserId, UserMessageConstants.SESSION_RESET_MESSAGE);
    }
}
