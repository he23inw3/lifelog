package jp.he23inw3.asset.adapter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Data;

/**
 * Slack Events API から送信されるイベントデータを受け取るための DTO クラス。
 * <p>
 * URL 検証用の challenge や、各種イベント情報を含みます。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackEventRequest {

    /**
     * トークン 文字列形式。
     */
    private String token;

    /**
     * チャレンジ URL 検証用の文字列。
     */
    private String challenge;

    /**
     * タイプ
     */
    private String type;

    /**
     * API アプリ ID 文字列形式。
     */
    @JsonProperty("api_app_id")
    private String apiAppId;

    /**
     * イベント オブジェクト形式。
     */
    private Map<String, Object> event;

    /**
     * イベント ID 文字列形式。
     */
    @JsonProperty("event_id")
    private String eventId;

    /**
     * イベント時刻 Long 形式。
     */
    @JsonProperty("event_time")
    private Long eventTime;
}
