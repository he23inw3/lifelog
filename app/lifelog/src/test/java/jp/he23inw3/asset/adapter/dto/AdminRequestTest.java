package jp.he23inw3.asset.adapter.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdminRequestTest {

    Validator validator;

    @BeforeEach
    void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Nested
    @DisplayName("バリデーション")
    class RequestValidation {

        @Test
        @DisplayName("有効なリクエストの場合はバリデーションエラーが発生しないこと")
        void success() {
            // Arrange
            var request = new AdminRequest();
            request.setActive(true);
            request.setUserName("Valid Admin Name");

            // Act
            var result = validator.validate(request);

            // Assert
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("管理者名が空文字・空白の場合にバリデーションエラーが発生すること")
        void userNameIsBlank() {
            // Arrange
            var request = new AdminRequest();
            request.setActive(true);
            request.setUserName("");

            // Act
            var result = validator.validate(request);

            // Assert
            assertThat(result.size()).isEqualTo(1);
            var errorMessages = result.stream().map(e -> e.getMessage()).toList();
            assertThat(errorMessages).contains("管理者名は必須です");
        }
    }
}
