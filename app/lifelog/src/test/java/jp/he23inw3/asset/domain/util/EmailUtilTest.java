package jp.he23inw3.asset.domain.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EmailUtilTest {

    @Nested
    @DisplayName("メールアドレスからのユーザー名抽出")
    class ExtractUserName {

        @Test
        @DisplayName("正常なメールアドレスからユーザー名を正しく抽出できること")
        void extractUserName_NormalEmail_Success() {
            String result = EmailUtil.extractUserName("test.user@example.com");
            assertThat(result).isEqualTo("test.user");
        }

        @Test
        @DisplayName("メールアドレスがnullの場合に空文字列を返すこと")
        void extractUserName_NullEmail_ReturnsEmptyString() {
            String result = EmailUtil.extractUserName(null);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("メールアドレスが空文字列の場合に空文字列を返すこと")
        void extractUserName_EmptyEmail_ReturnsEmptyString() {
            String result = EmailUtil.extractUserName("");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("メールアドレスに@が含まれない場合に文字列全体を返すこと")
        void extractUserName_NoAtSymbol_ReturnsWholeString() {
            String result = EmailUtil.extractUserName("testuser");
            assertThat(result).isEqualTo("testuser");
        }
    }
}
