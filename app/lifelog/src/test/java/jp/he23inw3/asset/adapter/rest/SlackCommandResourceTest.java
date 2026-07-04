package jp.he23inw3.asset.adapter.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import jp.he23inw3.asset.adapter.rest.command.LifeLogLinkCommandHandler;
import jp.he23inw3.asset.adapter.rest.command.SlackCommandHandler;
import jp.he23inw3.asset.usecase.SlackLinkageUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SlackCommandResourceTest {

    @Mock
    SlackLinkageUseCase slackLinkageUseCase;

    @Mock
    Instance<SlackCommandHandler> handlers;

    SlackCommandResource target;

    @BeforeEach
    void setUp() {
        LifeLogLinkCommandHandler handler = new LifeLogLinkCommandHandler(slackLinkageUseCase);
        when(handlers.iterator()).thenReturn(List.of((SlackCommandHandler) handler).iterator());
        target = new SlackCommandResource(handlers);
    }

    @Test
    @DisplayName("正しいコマンドを受信した場合にアカウント連携用トークンを生成してURLを返却できること")
    void handleCommand_Success() {
        when(slackLinkageUseCase.generateLinkageUrl("U123456"))
                .thenReturn("http://localhost:5173/settings?slackToken=test-token");

        Response response = target.handleCommand("/lifelog-link", "U123456", "john.doe");

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertThat(entity.get("response_type")).isEqualTo("ephemeral");
        assertThat((String) entity.get("text")).contains("this link");
        assertThat((String) entity.get("text")).contains("http://localhost:5173/settings?slackToken=test-token");
        verify(slackLinkageUseCase).generateLinkageUrl("U123456");
    }

    @Test
    @DisplayName("非対応のコマンドを受信した場合にエラーメッセージを返却すること")
    void handleCommand_UnsupportedCommand() {
        Response response = target.handleCommand("/unsupported", "U123456", "john.doe");

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertThat(entity.get("response_type")).isEqualTo("ephemeral");
        assertThat((String) entity.get("text")).contains("サポートされていないコマンドです");
        verifyNoInteractions(slackLinkageUseCase);
    }

    @Test
    @DisplayName("ユーザーIDが空の場合にBadRequestエラーとなること")
    void handleCommand_EmptyUserId_BadRequest() {
        Response response = target.handleCommand("/lifelog-link", "", "john.doe");
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verifyNoInteractions(slackLinkageUseCase);
    }
}
