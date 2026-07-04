package jp.he23inw3.asset.infrastructure.slack;

import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.Blocks.asBlocks;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asElements;
import static com.slack.api.model.block.element.BlockElements.button;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostEphemeralResponse;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsOpenResponse;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.webhook.Payload;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.exception.GatewayException;
import jp.he23inw3.asset.domain.gateway.SlackGateway;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Slack Web API（chat.postMessage）を使用した SlackGateway 実装。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SlackGatewayImpl implements SlackGateway {

    private final LifeLogConfig config;

    private final Slack slack;

    private MethodsClient methodsClient;

    @Override
    public void postMessage(String slackUserId, String text) {
        try {
            // DM チャネルを開く
            ConversationsOpenResponse openResponse = methodsClient
                    .conversationsOpen(r -> r.users(List.of(slackUserId)));

            if (!openResponse.isOk()) {
                throw new GatewayException("Slack DM チャネルのオープンに失敗しました: " + openResponse.getError());
            }

            String channelId = openResponse.getChannel().getId();

            ChatPostMessageResponse postResponse = methodsClient.chatPostMessage(r -> r.channel(channelId).text(text));

            if (!postResponse.isOk()) {
                throw new GatewayException("Slack メッセージの送信に失敗しました: " + postResponse.getError());
            }
            log.info(MessageHelper.getMessage("infra.slack.sent", slackUserId));
        } catch (IOException | SlackApiException e) {
            throw new GatewayException("Slack API の呼び出しに失敗しました。", e);
        }
    }

    @Override
    public void postConfirmationMessage(String slackUserId, String logDate, String tasks, double workHours,
            String diary, boolean isHoliday) {
        try {
            // DM チャネルを開く
            ConversationsOpenResponse openResponse = methodsClient
                    .conversationsOpen(r -> r.users(List.of(slackUserId)));

            if (!openResponse.isOk()) {
                throw new GatewayException("Slack DM チャネルのオープンに失敗しました: " + openResponse.getError());
            }

            String channelId = openResponse.getChannel().getId();
            List<LayoutBlock> blocks = createConfirmationBlocks(logDate, tasks, workHours, diary, isHoliday);

            ChatPostEphemeralResponse postResponse = methodsClient
                    .chatPostEphemeral(r -> r.channel(channelId).user(slackUserId).blocks(blocks));

            if (!postResponse.isOk()) {
                throw new GatewayException("Slack エフェメラルメッセージの送信に失敗しました: " + postResponse.getError());
            }
            log.info("Slack confirmation ephemeral message sent to {}", slackUserId);
        } catch (IOException | SlackApiException e) {
            throw new GatewayException("Slack API の呼び出しに失敗しました。", e);
        }
    }

    @Override
    public void updateMessage(String responseUrl, String text) {
        try {
            Payload payload = Payload.builder()
                    .text(text)
                    .build();
            slack.send(responseUrl, payload);
        } catch (IOException e) {
            throw new GatewayException("Slack メッセージの更新に失敗しました。", e);
        }
    }

    @PostConstruct
    void init() {
        methodsClient = slack.methods(config.slack().botToken());
        log.info(MessageHelper.getMessage("infra.slack.init"));
        log.info("--- Payload methods ---");
        for (Method m : Payload.class.getDeclaredMethods()) {
            log.info(m.toString());
        }
        log.info("--- PayloadBuilder methods ---");
        for (Method m : Payload.PayloadBuilder.class.getDeclaredMethods()) {
            log.info(m.toString());
        }
    }

    @PreDestroy
    void destroy() {
        // Shared Slack instance lifecycle is managed by the CDI container.
    }

    private List<LayoutBlock> createConfirmationBlocks(String logDate, String tasks, double workHours, String diary,
            boolean isHoliday) {
        String previewText = String.format("*📅 日付:* %s\n*🛠️ 業務:* %s\n*⏱️ 時間:* %.1fh\n*📔 日記:* %s",
                logDate != null ? logDate : "(未設定)", tasks != null && !tasks.isEmpty() ? tasks : "(なし)", workHours,
                diary != null && !diary.isEmpty() ? diary : "(なし)");

        return asBlocks(section(s -> s.text(markdownText("*🤖 Geminiが解析した日報プレビューです。確定しますか？*"))),
                section(s -> s.text(markdownText(previewText))),
                actions(a -> a.elements(asElements(
                        button(b -> b.text(plainText("これで確定する")).style("primary").actionId("approve_lifelog")),
                        button(b -> b.text(plainText("キャンセル")).style("danger").actionId("cancel_lifelog"))))));
    }
}
