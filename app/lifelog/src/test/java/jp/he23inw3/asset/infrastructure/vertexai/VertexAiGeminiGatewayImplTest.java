package jp.he23inw3.asset.infrastructure.vertexai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Schema;
import java.time.LocalDateTime;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.exception.GatewayException;
import jp.he23inw3.asset.domain.model.GeminiParseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VertexAiGeminiGatewayImplTest {

    @Mock
    LifeLogConfig config;

    @Mock
    LifeLogConfig.Gemini geminiConfig;

    @Mock
    LifeLogConfig.Google googleConfig;

    Client client;

    @Mock
    Models models;

    @Mock
    GenerateContentResponse response;

    @Mock
    Schema schema;

    @Mock
    GeminiPromptFactory promptFactory;

    @Mock
    GeminiResponseParser responseParser;

    VertexAiGeminiGatewayImpl target;

    @BeforeEach
    void setUp() throws Exception {
        when(config.gemini()).thenReturn(geminiConfig);
        when(geminiConfig.model()).thenReturn("gemini-1.5-flash");
        when(config.google()).thenReturn(googleConfig);
        when(googleConfig.projectId()).thenReturn("project-123");
        when(geminiConfig.location()).thenReturn("us-central1");

        when(promptFactory.createParseResponseSchema()).thenReturn(schema);

        // 本物の Client をダミーAPIキーでビルドし、Reflectionで final フィールド models にモックを代入する
        client = Client.builder().apiKey("dummy-key").build();
        java.lang.reflect.Field modelsField = Client.class.getDeclaredField("models");
        modelsField.setAccessible(true);
        modelsField.set(client, models);

        target = new VertexAiGeminiGatewayImpl(config, client, promptFactory, responseParser);
        target.init(); // @PostConstruct の呼び出しをシミュレート
    }

    @Nested
    @DisplayName("parseメソッドのテスト")
    class Parse {

        @Test
        @DisplayName("Gemini APIを呼び出し、結果が正常に解析されて返ること")
        void testParse_Success() throws Exception {
            String rawText = "開発業務を7.5時間行った。";
            LocalDateTime now = LocalDateTime.of(2026, 6, 30, 10, 0);
            String dayStatus = "WEEKDAY";

            when(promptFactory.createParsePrompt(rawText, now, dayStatus)).thenReturn("prompt");
            when(models.generateContent(eq("gemini-1.5-flash"), eq("prompt"), any(GenerateContentConfig.class)))
                    .thenReturn(response);
            when(response.text()).thenReturn("{\"sentiment\":\"HAPPY\"}");

            GeminiParseResult expected = GeminiParseResult.builder().build();
            when(responseParser.parse("{\"sentiment\":\"HAPPY\"}")).thenReturn(expected);

            GeminiParseResult result = target.parse(rawText, now, dayStatus);

            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("Geminiからの応答テキストが空の場合、GatewayExceptionを投げること")
        void testParse_EmptyResponse() throws Exception {
            String rawText = "開発業務";
            LocalDateTime now = LocalDateTime.of(2026, 6, 30, 10, 0);

            when(promptFactory.createParsePrompt(rawText, now, "WEEKDAY")).thenReturn("prompt");
            when(models.generateContent(eq("gemini-1.5-flash"), eq("prompt"), any(GenerateContentConfig.class)))
                    .thenReturn(response);
            when(response.text()).thenReturn("");

            assertThatThrownBy(() -> target.parse(rawText, now, "WEEKDAY"))
                    .isInstanceOf(GatewayException.class)
                    .hasMessageContaining(jp.he23inw3.asset.domain.constant.UserMessageConstants.GEMINI_PARSE_API_ERROR);
        }
    }

    @Nested
    @DisplayName("generateMonthlyReportメソッドのテスト")
    class GenerateMonthlyReport {

        @Test
        @DisplayName("振り返り用プロンプトを送信し、テキスト応答が正常に返ること")
        void testGenerateMonthlyReport_Success() throws Exception {
            String monthlySummary = "今月の稼働状況まとめ";
            when(promptFactory.createReflectionPrompt(monthlySummary)).thenReturn("reflect_prompt");
            when(models.generateContent(eq("gemini-1.5-flash"), eq("reflect_prompt"), any(GenerateContentConfig.class)))
                    .thenReturn(response);
            when(response.text()).thenReturn("素晴らしい月でした。");

            String result = target.generateMonthlyReport(monthlySummary);

            assertThat(result).isEqualTo("素晴らしい月でした。");
        }
    }
}
