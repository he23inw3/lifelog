package jp.he23inw3.asset.infrastructure.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestConnectorTest {

    @Mock
    HttpClient httpClient;
    @Mock
    HttpResponse<String> httpResponse;

    RestConnector target;

    static class TestDto {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        target = new RestConnector(objectMapper);
        target.init();

        // Reflectionで HttpClient をモックに差し替え
        java.lang.reflect.Field clientField = RestConnector.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(target, httpClient);
    }

    @Nested
    @DisplayName("getメソッドのテスト")
    class Get {

        @Test
        @DisplayName("GETリクエストが正常に終了し文字列レスポンスが返ること")
        void testGet_String() throws Exception {
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn("response-body");

            String result = target.get("http://example.com");

            assertThat(result).isEqualTo("response-body");
        }

        @Test
        @DisplayName("GETリクエストが正常に終了し指定のDTOオブジェクトにデシリアライズされること")
        void testGet_Dto() throws Exception {
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn("{\"name\":\"get-test\"}");

            TestDto result = target.get("http://example.com", TestDto.class);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("get-test");
        }

        @Test
        @DisplayName("HTTPステータスがエラーの場合、IOExceptionを投げること")
        void testGet_HttpError() throws Exception {
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
            when(httpResponse.statusCode()).thenReturn(404);
            when(httpResponse.body()).thenReturn("Not Found");

            assertThatThrownBy(() -> target.get("http://example.com"))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("HTTP error: status=404");
        }
    }

    @Nested
    @DisplayName("postメソッドのテスト")
    class Post {

        @Test
        @DisplayName("POSTリクエストが正常に終了しリクエストボディとレスポンスボディが処理されること")
        void testPost_Success() throws Exception {
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
            when(httpResponse.statusCode()).thenReturn(201);
            when(httpResponse.body()).thenReturn("{\"name\":\"created\"}");

            TestDto requestBody = new TestDto();
            requestBody.setName("new-item");

            TestDto result = target.post("http://example.com", requestBody, TestDto.class);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("created");
        }
    }

    @Nested
    @DisplayName("putメソッドのテスト")
    class Put {

        @Test
        @DisplayName("PUTリクエストが正常に終了し更新結果が返ること")
        void testPut_Success() throws Exception {
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn("{\"name\":\"updated\"}");

            TestDto requestBody = new TestDto();
            requestBody.setName("update-item");

            TestDto result = target.put("http://example.com", requestBody, TestDto.class);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("updated");
        }
    }

    @Nested
    @DisplayName("deleteメソッドのテスト")
    class Delete {

        @Test
        @DisplayName("DELETEリクエストが正常に終了すること")
        void testDelete_Success() throws Exception {
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
            when(httpResponse.statusCode()).thenReturn(204);
            when(httpResponse.body()).thenReturn("");

            TestDto result = target.delete("http://example.com", TestDto.class);

            assertThat(result).isNull();
        }
    }
}
