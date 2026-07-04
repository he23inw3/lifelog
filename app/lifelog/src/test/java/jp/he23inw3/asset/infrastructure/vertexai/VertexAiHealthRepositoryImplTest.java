package jp.he23inw3.asset.infrastructure.vertexai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentResponse;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.model.HealthStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VertexAiHealthRepositoryImplTest {

    @Mock
    LifeLogConfig config;

    @Mock
    Client client;

    @Mock
    Models models;

    @InjectMocks
    VertexAiHealthRepositoryImpl target;

    @BeforeEach
    void setUp() throws Exception {
        java.lang.reflect.Field modelsField = Client.class.getDeclaredField("models");
        modelsField.setAccessible(true);
        modelsField.set(client, models);
    }

    @Nested
    @DisplayName("ヘルスチェック")
    class CheckHealth {

        @Test
        @DisplayName("Vertex AI Gemini APIの疎通チェック正常時、UPを返すこと")
        void checkHealth_Success_ShouldReturnUp() {
            // Arrange
            LifeLogConfig.Gemini geminiConfig = mock(LifeLogConfig.Gemini.class);
            when(config.gemini()).thenReturn(geminiConfig);
            when(geminiConfig.model()).thenReturn("gemini-1.5-flash");

            GenerateContentResponse response = mock(GenerateContentResponse.class);
            when(models.generateContent("gemini-1.5-flash", "ping", null)).thenReturn(response);

            // Act
            HealthStatus actual = target.checkHealth();

            // Assert
            assertThat(actual).isEqualTo(HealthStatus.UP);
            assertThat(target.getServiceName()).isEqualTo("Vertex AI");
        }

        @Test
        @DisplayName("Vertex AI Gemini APIの疎通チェック例外発生時、DOWNを返すこと")
        void checkHealth_Failure_ShouldReturnDown() {
            // Arrange
            LifeLogConfig.Gemini geminiConfig = mock(LifeLogConfig.Gemini.class);
            when(config.gemini()).thenReturn(geminiConfig);
            when(geminiConfig.model()).thenReturn("gemini-1.5-flash");

            when(models.generateContent("gemini-1.5-flash", "ping", null)).thenThrow(new RuntimeException("API error"));

            // Act
            HealthStatus actual = target.checkHealth();

            // Assert
            assertThat(actual).isEqualTo(HealthStatus.DOWN);
        }
    }
}
