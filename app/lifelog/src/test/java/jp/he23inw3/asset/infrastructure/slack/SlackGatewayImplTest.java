package jp.he23inw3.asset.infrastructure.slack;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.chat.ChatPostEphemeralResponse;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsOpenResponse;
import com.slack.api.model.Conversation;
import com.slack.api.webhook.Payload;
import java.io.IOException;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.exception.GatewayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SlackGatewayImplTest {

    @Mock
    LifeLogConfig config;

    @Mock
    LifeLogConfig.Slack slackConfig;

    @Mock
    Slack slack;

    @Mock
    MethodsClient methodsClient;

    SlackGatewayImpl target;

    @BeforeEach
    void setUp() {
        when(config.slack()).thenReturn(slackConfig);
        when(slackConfig.botToken()).thenReturn("bot-token");
        when(slack.methods("bot-token")).thenReturn(methodsClient);

        target = new SlackGatewayImpl(config, slack);
        target.init(); // @PostConstruct の呼び出しをシミュレート
    }

    @Nested
    @DisplayName("postMessageメソッドのテスト")
    class PostMessage {

        @Test
        @DisplayName("DMチャネルを開いて正常にメッセージを送信できること")
        void testPostMessage_Success() throws Exception {
            String slackUserId = "U12345";
            String text = "テストメッセージ";

            ConversationsOpenResponse openResponse = mock(ConversationsOpenResponse.class);
            when(openResponse.isOk()).thenReturn(true);
            Conversation conversation = mock(Conversation.class);
            when(openResponse.getChannel()).thenReturn(conversation);
            when(conversation.getId()).thenReturn("C123");

            ChatPostMessageResponse postResponse = mock(ChatPostMessageResponse.class);
            when(postResponse.isOk()).thenReturn(true);

            when(methodsClient.conversationsOpen(any(com.slack.api.RequestConfigurator.class)))
                    .thenReturn(openResponse);
            when(methodsClient.chatPostMessage(any(com.slack.api.RequestConfigurator.class))).thenReturn(postResponse);

            target.postMessage(slackUserId, text);

            verify(methodsClient).conversationsOpen(any(com.slack.api.RequestConfigurator.class));
            verify(methodsClient).chatPostMessage(any(com.slack.api.RequestConfigurator.class));
        }

        @Test
        @DisplayName("DMチャネルのオープンに失敗した場合、GatewayExceptionを投げること")
        void testPostMessage_OpenFail() throws Exception {
            String slackUserId = "U12345";
            String text = "テストメッセージ";

            ConversationsOpenResponse openResponse = mock(ConversationsOpenResponse.class);
            when(openResponse.isOk()).thenReturn(false);
            when(openResponse.getError()).thenReturn("user_not_found");

            when(methodsClient.conversationsOpen(any(com.slack.api.RequestConfigurator.class)))
                    .thenReturn(openResponse);

            assertThatThrownBy(() -> target.postMessage(slackUserId, text))
                    .isInstanceOf(GatewayException.class)
                    .hasMessageContaining("Slack DM チャネルのオープンに失敗しました: user_not_found");
        }
    }

    @Nested
    @DisplayName("postConfirmationMessageメソッドのテスト")
    class PostConfirmationMessage {

        @Test
        @DisplayName("確認用のエフェメラルブロックメッセージを正常に送信できること")
        void testPostConfirmationMessage_Success() throws Exception {
            String slackUserId = "U12345";

            ConversationsOpenResponse openResponse = mock(ConversationsOpenResponse.class);
            when(openResponse.isOk()).thenReturn(true);
            Conversation conversation = mock(Conversation.class);
            when(openResponse.getChannel()).thenReturn(conversation);
            when(conversation.getId()).thenReturn("C123");

            ChatPostEphemeralResponse postResponse = mock(ChatPostEphemeralResponse.class);
            when(postResponse.isOk()).thenReturn(true);

            when(methodsClient.conversationsOpen(any(com.slack.api.RequestConfigurator.class)))
                    .thenReturn(openResponse);
            when(methodsClient.chatPostEphemeral(any(com.slack.api.RequestConfigurator.class)))
                    .thenReturn(postResponse);

            target.postConfirmationMessage(slackUserId, "2026-06-30", "タスク", 7.5, "日記", false);

            verify(methodsClient).conversationsOpen(any(com.slack.api.RequestConfigurator.class));
            verify(methodsClient).chatPostEphemeral(any(com.slack.api.RequestConfigurator.class));
        }
    }

    @Nested
    @DisplayName("updateMessageメソッドのテスト")
    class UpdateMessage {

        @Test
        @DisplayName("responseUrlを介して正常にメッセージを更新・返信できること")
        void testUpdateMessage_Success() throws Exception {
            String responseUrl = "http://response.slack.com";
            String text = "更新テキスト";

            target.updateMessage(responseUrl, text);

            verify(slack).send(eq(responseUrl), any(Payload.class));
        }

        @Test
        @DisplayName("メッセージ更新でIOExceptionが起きた場合、GatewayExceptionを投げること")
        void testUpdateMessage_ThrowsException() throws Exception {
            String responseUrl = "http://response.slack.com";
            String text = "更新テキスト";

            doThrow(new IOException("network error")).when(slack).send(eq(responseUrl), any(Payload.class));

            assertThatThrownBy(() -> target.updateMessage(responseUrl, text))
                    .isInstanceOf(GatewayException.class)
                    .hasMessageContaining("Slack メッセージの更新に失敗しました。");
        }
    }
}
