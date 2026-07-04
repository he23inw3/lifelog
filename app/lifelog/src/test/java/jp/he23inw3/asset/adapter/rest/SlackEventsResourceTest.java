package jp.he23inw3.asset.adapter.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import jp.he23inw3.asset.adapter.dto.SlackEventRequest;
import jp.he23inw3\u002easset.adapter.rest.event.MessageEventHandler;
import jp.he23inw3\u002easset.adapter.rest.event.SlackEventHandler;
import jp.he23inw3\u002easset.adapter.rest.interaction.ApproveLifelogInteractionHandler;
import jp.he23inw3\u002easset.adapter.rest.interaction.CancelLifelogInteractionHandler;
import jp.he23inw3\u002easset.adapter.rest.interaction.SlackInteractionHandler;
import jp.he23inw3\u002easset.usecase.LifelogWorkflowUseCase;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SlackEventsResourceTest {

    @Mock
    LifelogWorkflowUseCase workflowUseCase;

    @Mock
    ManagedExecutor managedExecutor;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    Instance<SlackEventHandler> eventHandlers;

    @Mock
    Instance<SlackInteractionHandler> interactionHandlers;

    SlackEventsResource target;

    @BeforeEach
    void setUp() {
        // Stub managedExecutor.submit to execute the runnable synchronously
        lenient().when(managedExecutor.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return mock(Future.class);
        });

        // Set up the event handlers
        MessageEventHandler messageEventHandler = new MessageEventHandler(workflowUseCase);
        lenient().when(eventHandlers.iterator())
                .thenReturn(List.of((SlackEventHandler) messageEventHandler).iterator());

        // Set up the interaction handlers
        ApproveLifelogInteractionHandler approveHandler = new ApproveLifelogInteractionHandler(workflowUseCase);
        CancelLifelogInteractionHandler cancelHandler = new CancelLifelogInteractionHandler(workflowUseCase);
        lenient().when(interactionHandlers.iterator()).thenReturn(
                List.of((SlackInteractionHandler) approveHandler, cancelHandler).iterator());

        target = new SlackEventsResource(eventHandlers, interactionHandlers, managedExecutor, objectMapper);
    }

    @Test
    @DisplayName("url_verificationイベント受信時にchallenge値を返却すること")
    void handleEvent_UrlVerification_Success() {
        // Arrange
        SlackEventRequest request = new SlackEventRequest();
        request.setType("url_verification");
        request.setChallenge("test-challenge-123");

        // Act
        Response response = target.handleEvent(null, null, request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(200);
        Map<String, String> entity = (Map<String, String>) response.getEntity();
        assertThat(entity.get("challenge")).isEqualTo("test-challenge-123");
        verifyNoInteractions(workflowUseCase);
    }

    @Test
    @DisplayName("Bot自身以外のメッセージイベント受信時にprocessLifelogが呼び出されること")
    void handleEvent_MessageFromUser_ShouldProcessLifelog() {
        // Arrange
        SlackEventRequest request = new SlackEventRequest();
        request.setType("event_callback");
        Map<String, Object> event = new HashMap<>();
        event.put("type", "message");
        event.put("user", "U123456");
        event.put("text", "日報タスクA 8h");
        event.put("bot_id", null); // not from bot
        request.setEvent(event);

        // Act
        Response response = target.handleEvent(null, null, request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(200);
        verify(workflowUseCase).processLifelog("U123456", "日報タスクA 8h");
    }

    @Test
    @DisplayName("Botからのメッセージイベント受信時は処理をスキップすること")
    void handleEvent_MessageFromBot_ShouldSkip() {
        // Arrange
        SlackEventRequest request = new SlackEventRequest();
        request.setType("event_callback");
        Map<String, Object> event = new HashMap<>();
        event.put("type", "message");
        event.put("user", "U123456");
        event.put("text", "Bot message");
        event.put("bot_id", "B777"); // from bot
        request.setEvent(event);

        // Act
        Response response = target.handleEvent(null, null, request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(200);
        verifyNoInteractions(workflowUseCase);
    }

    @Test
    @DisplayName("無効なイベントタイプの受信時は処理をスキップすること")
    void handleEvent_UnknownType_ShouldSkip() {
        // Arrange
        SlackEventRequest request = new SlackEventRequest();
        request.setType("unknown_type");

        // Act
        Response response = target.handleEvent(null, null, request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(200);
        verifyNoInteractions(workflowUseCase);
    }

    @Test
    @DisplayName("リトライヘッダー付きでイベントを受信した際、警告ログを出力しつつ処理を実行できること")
    void handleEvent_WithRetryHeader_Success() {
        // Arrange
        SlackEventRequest request = new SlackEventRequest();
        request.setType("url_verification");
        request.setChallenge("test-challenge-123");

        // Act
        Response response = target.handleEvent("1", "http_timeout", request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(200);
        Map<String, String> entity = (Map<String, String>) response.getEntity();
        assertThat(entity.get("challenge")).isEqualTo("test-challenge-123");
        verifyNoInteractions(workflowUseCase);
    }

    @Test
    @DisplayName("インタラクション受信時：payloadが空ならBadRequestを返却すること")
    void handleInteraction_NullOrEmptyPayload_ShouldReturnBadRequest() {
        // Act & Assert
        Response responseNull = target.handleInteraction(null);
        assertThat(responseNull.getStatus()).isEqualTo(400);

        Response responseEmpty = target.handleInteraction("");
        assertThat(responseEmpty.getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("インタラクション受信時：無効なJSONペイロードならBadRequestを返却すること")
    void handleInteraction_InvalidJson_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String invalidPayload = "{invalid}";
        when(objectMapper.readTree(invalidPayload)).thenThrow(new RuntimeException("JSON error"));

        // Act
        Response response = target.handleInteraction(invalidPayload);

        // Assert
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("インタラクション受信時：確定アクション (approve_lifelog) の処理が正常に行われること")
    void handleInteraction_ApproveLifelog_Success() throws Exception {
        // Arrange
        String payload = "{}";
        JsonNode root = mock(JsonNode.class);
        JsonNode userNode = mock(JsonNode.class);
        JsonNode actionNode = mock(JsonNode.class);
        JsonNode actionsNode = mock(JsonNode.class);

        JsonNode typeNode = mockJsonNode("block_actions");
        JsonNode userIdNode = mockJsonNode("U123456");
        JsonNode responseUrlNode = mockJsonNode("http://response.url");
        JsonNode actionIdNode = mockJsonNode("approve_lifelog");

        when(objectMapper.readTree(payload)).thenReturn(root);
        when(root.path("type")).thenReturn(typeNode);
        when(root.path("user")).thenReturn(userNode);
        when(userNode.path("id")).thenReturn(userIdNode);
        when(root.path("response_url")).thenReturn(responseUrlNode);
        when(root.path("actions")).thenReturn(actionsNode);
        when(actionsNode.path(0)).thenReturn(actionNode);
        when(actionNode.path("action_id")).thenReturn(actionIdNode);

        // Act
        Response response = target.handleInteraction(payload);

        // Assert
        assertThat(response.getStatus()).isEqualTo(200);
        verify(workflowUseCase).confirmRegistration("U123456", "http://response.url");
    }

    @Test
    @DisplayName("インタラクション受信時：キャンセルアクション (cancel_lifelog) の処理が正常に行われること")
    void handleInteraction_CancelLifelog_Success() throws Exception {
        // Arrange
        String payload = "{}";
        JsonNode root = mock(JsonNode.class);
        JsonNode userNode = mock(JsonNode.class);
        JsonNode actionNode = mock(JsonNode.class);
        JsonNode actionsNode = mock(JsonNode.class);

        JsonNode typeNode = mockJsonNode("block_actions");
        JsonNode userIdNode = mockJsonNode("U123456");
        JsonNode responseUrlNode = mockJsonNode("http://response.url");
        JsonNode actionIdNode = mockJsonNode("cancel_lifelog");

        when(objectMapper.readTree(payload)).thenReturn(root);
        when(root.path("type")).thenReturn(typeNode);
        when(root.path("user")).thenReturn(userNode);
        when(userNode.path("id")).thenReturn(userIdNode);
        when(root.path("response_url")).thenReturn(responseUrlNode);
        when(root.path("actions")).thenReturn(actionsNode);
        when(actionsNode.path(0)).thenReturn(actionNode);
        when(actionNode.path("action_id")).thenReturn(actionIdNode);

        // Act
        Response response = target.handleInteraction(payload);

        // Assert
        assertThat(response.getStatus()).isEqualTo(200);
        verify(workflowUseCase).cancelRegistration("U123456", "http://response.url");
    }

    private JsonNode mockJsonNode(String textValue) {
        JsonNode node = mock(JsonNode.class);
        lenient().when(node.asText()).thenReturn(textValue);
        return node;
    }
}
