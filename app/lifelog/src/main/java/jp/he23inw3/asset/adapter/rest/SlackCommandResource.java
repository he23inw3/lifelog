package jp.he23inw3.asset.adapter.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import jp.he23inw3.asset.adapter.constant.ApiPath;
import jp.he23inw3.asset.adapter.constant.ApiTag;
import jp.he23inw3.asset.adapter.rest.command.SlackCommandHandler;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Slack からの Slash Command を処理する REST リソースクラス。
 */
@Slf4j
@Path(ApiPath.SLACK_COMMANDS)
@Tag(name = ApiTag.SLACK)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@ApplicationScoped
public class SlackCommandResource {

    private final Instance<SlackCommandHandler> handlers;

    /**
     * Slack Slash Command を受信し、対応するハンドラーに処理を委譲します。
     *
     * @param command 実行されたコマンド名
     * @param userId Slack ユーザーID
     * @param userName Slack ユーザー名
     * @return Slack へのレスポンス (ephemeral メッセージ)
     */
    @POST
    @Operation(operationId = "BE-API303", summary = "Slack Slash Command受信", description = "Slack からの Slash Command を受信し、アカウント連携用リンクなどを返します。")
    public Response handleCommand(
            @FormParam("command") String command,
            @FormParam("user_id") String userId,
            @FormParam("user_name") String userName) {

        log.info(MessageHelper.getMessage("adapter.slack.command.received", command, userId, userName));

        for (SlackCommandHandler handler : handlers) {
            if (handler.canHandle(command)) {
                return handler.handle(command, userId, userName);
            }
        }

        log.warn(MessageHelper.getMessage("adapter.slack.command.unsupported.log", command));
        return Response.ok(Map.of("response_type", "ephemeral", "text",
                MessageHelper.getMessage("adapter.slack.command.unsupported.message", command))).build();
    }
}
