package jp.he23inw3.asset.adapter.rest.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import jp.he23inw3.asset.usecase.LifelogWorkflowUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 日報登録確定アクションを処理するハンドラー。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ApproveLifelogInteractionHandler implements SlackInteractionHandler {

    private final LifelogWorkflowUseCase workflowUseCase;

    @Override
    public boolean canHandle(String type, String actionId) {
        return "block_actions".equals(type) && "approve_lifelog".equals(actionId);
    }

    @Override
    public void handle(JsonNode root) {
        String slackUserId = root.path("user").path("id").asText();
        String responseUrl = root.path("response_url").asText();

        try {
            workflowUseCase.confirmRegistration(slackUserId, responseUrl);
        } catch (Exception e) {
            log.error("Error processing Slack interaction for user " + slackUserId, e);
        }
    }
}
