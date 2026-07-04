package jp.he23inw3.asset.adapter.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import jp.he23inw3.asset.adapter.constant.ApiPath;
import jp.he23inw3.asset.adapter.constant.ApiTag;
import jp.he23inw3.asset.adapter.dto.SlackEventRequest;
import jp.he23inw3.asset.adapter.rest.event.SlackEventHandler;
import jp.he23inw3.asset.adapter.rest.interaction.SlackInteractionHandler;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Slack Events API から送信される Webhook イベントを処理する REST リソースクラス。
 * <p>
 * Slack 疎通確認用ハンドシェイク (url_verification) への応答、および 受信したメッセージイベントの非同期処理を行います。
 */
@Slf4j
@Path(ApiPath.SLACK_EVENTS)
@Tag(name = ApiTag.SLACK)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@ApplicationScoped
public class SlackEventsResource {

    private final Instance<SlackEventHandler> eventHandlers;

    private final Instance<SlackInteractionHandler> interactionHandlers;

    private final ManagedExecutor managedExecutor;

    private final ObjectMapper objectMapper;

    /**
     * Slack Events API からの Webhook リクエストを処理します。
     *
     * @param retryNum リトライ回数
     * @param retryReason リトライ理由
     * @param request Slack から送信されたイベントデータ (url_verification または event_callback)
     * @return 疎通確認時は challenge を含む応答、それ以外は即座に HTTP 200 OK の空応答
     */
    @POST
    @Operation(operationId = "BE-API301", summary = "Slackイベント受信", description = "Slack Events API からの Webhook イベントを受信・処理します。")
    public Response handleEvent(@HeaderParam("X-Slack-Retry-Num") String retryNum,
            @HeaderParam("X-Slack-Retry-Reason") String retryReason, SlackEventRequest request) {
        if (StringUtils.isNotEmpty(retryNum)) {
            log.warn(MessageHelper.getMessage("adapter.rest.slack.retry.warn", retryNum, retryReason));
        }

        // URL Verification (Slackの疎通確認ハンドシェイク)
        if ("url_verification".equals(request.getType())) {
            log.info(MessageHelper.getMessage("adapter.rest.slack.verification"));
            return Response.ok(Map.of("challenge", request.getChallenge())).build();
        }

        // イベント処理（非同期化して3秒ルールを回避）
        if ("event_callback".equals(request.getType()) && request.getEvent() != null) {
            Map<String, Object> event = request.getEvent();
            String type = (String) event.get("type");

            for (SlackEventHandler handler : eventHandlers) {
                if (handler.canHandle(type, event)) {
                    managedExecutor.submit(() -> {
                        try {
                            handler.handle(event);
                        } catch (Exception e) {
                            log.error(MessageHelper.getMessage("adapter.rest.slack.event.handler.error", type), e);
                        }
                    });
                    break;
                }
            }
        }

        // 即座に 200 OK を返す
        return Response.ok().build();
    }

    /**
     * Slack ボタン押下やドロップダウン選択などのインタラクションイベントを処理します。
     *
     * @param payload Slack から URL エンコードされたフォームパラメータとして送信された JSON 文字列
     * @return 即座に HTTP 200 OK
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(operationId = "BE-API302", summary = "Slackインタラクション受信", description = "Slack ボタン押下やドロップダウン選択を受信します。")
    public Response handleInteraction(@FormParam("payload") String payload) {
        if (StringUtils.isEmpty(payload)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            String type = root.path("type").asText();

            String actionId = "block_actions".equals(type)
                    ? root.path("actions").path(0).path("action_id").asText()
                    : null;

            for (SlackInteractionHandler handler : interactionHandlers) {
                if (handler.canHandle(type, actionId)) {
                    managedExecutor.submit(() -> {
                        try {
                            handler.handle(root);
                        } catch (Exception e) {
                            log.error(MessageHelper.getMessage("adapter.rest.slack.interaction.handler.error",
                                    type, actionId), e);
                        }
                    });
                    return Response.ok().build();
                }
            }
        } catch (Exception e) {
            log.error(MessageHelper.getMessage("adapter.rest.slack.interaction.parse.error"), e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return Response.ok().build();
    }
}
