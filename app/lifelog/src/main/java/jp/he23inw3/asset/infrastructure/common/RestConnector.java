package jp.he23inw3.asset.infrastructure.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 軽量 REST HTTP クライアント。 Java 標準の HttpClient をラップし、JSON 取得を簡略化する。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class RestConnector {

    private final ObjectMapper objectMapper;

    private HttpClient client;

    private ObjectMapper localObjectMapper;

    /**
     * GET リクエストを送信してレスポンスボディを文字列で返す。
     *
     * @param url リクエスト先 URL
     * @return レスポンスボディ
     */
    public String get(String url) throws IOException, InterruptedException {
        return get(url, String.class);
    }

    /**
     * GET リクエストを送信してレスポンスボディを指定の型で返す。
     * 
     * @param url リクエスト先 URL
     * @param responseType レスポンスボディの型
     * @param <T> レスポンスボディの型
     * @return レスポンスボディ
     * @throws IOException HTTPエラー
     * @throws InterruptedException 通信エラー
     */
    public <T> T get(String url, Class<T> responseType) throws IOException, InterruptedException {
        log.debug(MessageHelper.getMessage("rest.get", url));
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json").GET().build();

        return sendRequest(request, responseType);
    }

    /**
     * POST リクエストを送信してレスポンスボディを指定の型で返す。
     * 
     * @param url リクエスト先 URL
     * @param requestBody リクエストボディ
     * @param responseType レスポンスボディの型
     * @param <T> レスポンスボディの型
     * @return レスポンスボディ
     * @throws IOException HTTPエラー
     * @throws InterruptedException 通信エラー
     */
    public <T> T post(String url, Object requestBody, Class<T> responseType) throws IOException, InterruptedException {
        log.debug(MessageHelper.getMessage("rest.post", url));
        String jsonBody = requestBody != null ? localObjectMapper.writeValueAsString(requestBody) : "";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendRequest(request, responseType);
    }

    /**
     * PUT リクエストを送信してレスポンスボディを指定の型で返す。
     * 
     * @param url リクエスト先 URL
     * @param requestBody リクエストボディ
     * @param responseType レスポンスボディの型
     * @param <T> レスポンスボディの型
     * @return レスポンスボディ
     * @throws IOException HTTPエラー
     * @throws InterruptedException 通信エラー
     */
    public <T> T put(String url, Object requestBody, Class<T> responseType) throws IOException, InterruptedException {
        log.debug(MessageHelper.getMessage("rest.put", url));
        String jsonBody = requestBody != null ? localObjectMapper.writeValueAsString(requestBody) : "";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendRequest(request, responseType);
    }

    /**
     * DELETE リクエストを送信してレスポンスボディを指定の型で返す。
     * 
     * @param url リクエスト先 URL
     * @param responseType レスポンスボディの型
     * @param <T> レスポンスボディの型
     * @return レスポンスボディ
     * @throws IOException HTTPエラー
     * @throws InterruptedException 通信エラー
     */
    public <T> T delete(String url, Class<T> responseType) throws IOException, InterruptedException {
        log.debug(MessageHelper.getMessage("rest.delete", url));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .DELETE()
                .build();

        return sendRequest(request, responseType);
    }

    @PostConstruct
    void init() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        // インジェクションされた共有 ObjectMapper を壊さないようコピーしてカスタム設定を適用
        this.localObjectMapper = this.objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private <T> T sendRequest(HttpRequest request, Class<T> responseType) throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.debug(MessageHelper.getMessage("rest.response", response.statusCode(), request.uri()));

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("HTTP error: status=" + response.statusCode() + ", uri=" + request.uri() + ", body="
                    + response.body());
        }

        if (responseType == String.class) {
            return responseType.cast(response.body());
        }

        if (StringUtils.isBlank(response.body())) {
            return null;
        }

        return localObjectMapper.readValue(response.body(), responseType);
    }
}
