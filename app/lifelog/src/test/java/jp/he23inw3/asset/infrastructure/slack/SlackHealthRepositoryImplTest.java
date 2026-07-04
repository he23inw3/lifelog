package jp.he23inw3.asset.infrastructure.slack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.auth.AuthTestResponse;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.model.HealthStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SlackHealthRepositoryImplTest {

    @Mock
    LifeLogConfig config;

    @Mock
    Slack slack;

    @InjectMocks
    SlackHealthRepositoryImpl target;

    @Test
    @DisplayName("Slack APIの疎通チェック正常時、UPを返すこと")
    void checkHealth_Success_ShouldReturnUp() throws Exception {
        // Arrange
        LifeLogConfig.Slack slackConfig = mock(LifeLogConfig.Slack.class);
        when(config.slack()).thenReturn(slackConfig);
        when(slackConfig.botToken()).thenReturn("xoxb-dummy-token");

        MethodsClient methodsClient = mock(MethodsClient.class);
        when(slack.methods("xoxb-dummy-token")).thenReturn(methodsClient);

        AuthTestResponse response = mock(AuthTestResponse.class);
        when(response.isOk()).thenReturn(true);
        when(methodsClient.authTest(any(com.slack.api.RequestConfigurator.class))).thenReturn(response);

        // Act
        HealthStatus actual = target.checkHealth();

        // Assert
        assertThat(actual).isEqualTo(HealthStatus.UP);
        assertThat(target.getServiceName()).isEqualTo("Slack");
        verify(methodsClient).authTest(any(com.slack.api.RequestConfigurator.class));
    }

    @Test
    @DisplayName("Slack APIの疎通チェックレスポンスが異常(isOk=false)の場合、DOWNを返すこと")
    void checkHealth_ResponseNotOk_ShouldReturnDown() throws Exception {
        // Arrange
        LifeLogConfig.Slack slackConfig = mock(LifeLogConfig.Slack.class);
        when(config.slack()).thenReturn(slackConfig);
        when(slackConfig.botToken()).thenReturn("xoxb-dummy-token");

        MethodsClient methodsClient = mock(MethodsClient.class);
        when(slack.methods("xoxb-dummy-token")).thenReturn(methodsClient);

        AuthTestResponse response = mock(AuthTestResponse.class);
        when(response.isOk()).thenReturn(false);
        when(response.getError()).thenReturn("invalid_auth");
        when(methodsClient.authTest(any(com.slack.api.RequestConfigurator.class))).thenReturn(response);

        // Act
        HealthStatus actual = target.checkHealth();

        // Assert
        assertThat(actual).isEqualTo(HealthStatus.DOWN);
    }

    @Test
    @DisplayName("Slack API接続中に例外が発生した場合、DOWNを返すこと")
    void checkHealth_Exception_ShouldReturnDown() throws Exception {
        // Arrange
        LifeLogConfig.Slack slackConfig = mock(LifeLogConfig.Slack.class);
        when(config.slack()).thenReturn(slackConfig);
        when(slackConfig.botToken()).thenReturn("xoxb-dummy-token");

        MethodsClient methodsClient = mock(MethodsClient.class);
        when(slack.methods("xoxb-dummy-token")).thenReturn(methodsClient);
        when(methodsClient.authTest(any(com.slack.api.RequestConfigurator.class)))
                .thenThrow(new RuntimeException("Slack connection failed"));

        // Act
        HealthStatus actual = target.checkHealth();

        // Assert
        assertThat(actual).isEqualTo(HealthStatus.DOWN);
    }
}
