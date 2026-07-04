package jp.he23inw3.asset.adapter.rest.event;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import jp.he23inw3.asset.usecase.LifelogWorkflowUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * ユーザーからのメッセージイベントを処理するハンドラー。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class MessageEventHandler implements SlackEventHandler {

    private final LifelogWorkflowUseCase workflowUseCase;

    @Override
    public boolean canHandle(String eventType, Map<String, Object> eventData) {
        return "message".equals(eventType) && eventData.get("bot_id") == null;
    }

    @Override
    public void handle(Map<String, Object> eventData) {
        String user = (String) eventData.get("user");
        String text = (String) eventData.get("text");

        if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(text)) {
            try {
                workflowUseCase.processLifelog(user, text);
            } catch (Exception e) {
                log.error(MessageHelper.getMessage("adapter.rest.slack.workflow.error", user), e);
            }
        }
    }
}
