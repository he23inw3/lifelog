package jp.he23inw3.asset.infrastructure.vertexai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import jp.he23inw3.asset.domain.model.GeminiParseResult;
import jp.he23inw3.asset.domain.model.Sentiment;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Gemini から返却された JSON レスポンスをパースして GeminiParseResult に変換するクラス。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class GeminiResponseParser {

    private final ObjectMapper objectMapper;

    /**
     * JSON 文字列をパースして GeminiParseResult に変換します。
     *
     * @param json
     *            Gemini から返却された JSON 文字列
     * @return 解析結果オブジェクト
     * @throws IOException
     *             パースエラーが発生した場合
     */
    public GeminiParseResult parse(String json) throws IOException {
        try {
            JsonNode node = objectMapper.readTree(json);

            JsonNode sentimentNode = node.path("sentiment");
            String sentimentText = (sentimentNode.isMissingNode() || sentimentNode.isNull())
                    ? "Neutral"
                    : sentimentNode.asText();

            JsonNode replyNode = node.path("reply_message");
            String replyMessageText = (replyNode.isMissingNode() || replyNode.isNull())
                    ? "記録しました。"
                    : replyNode.asText();

            return GeminiParseResult.builder()
                    .logRelated(node.path("log_related").asBoolean(false))
                    .logDate(node.path("log_date").asText()).holiday(node.path("holiday").asBoolean(false))
                    .tasks(node.path("tasks").asText()).workHours(node.path("work_hours").asDouble(0.0))
                    .overtimeHours(node.path("overtime_hours").asDouble(0.0)).diary(node.path("diary").asText())
                    .sentiment(Sentiment.fromValue(sentimentText)).replyMessage(replyMessageText).build();
        } catch (Exception e) {
            log.error(MessageHelper.getMessage("infra.gemini.json.parse.error", json), e);
            throw e;
        }
    }
}
