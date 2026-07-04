package jp.he23inw3.asset.adapter.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserSettingRequestTest {

    private Validator validator;

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
            UserSettingRequest request = createValidRequest();

            // Act
            var result = validator.validate(request);

            // Assert
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("ユーザー名が空文字・空白の場合にバリデーションエラーが発生すること")
        void userNameIsBlank() {
            // Arrange
            UserSettingRequest request = createValidRequest();
            request.setUserName("   ");

            // Act
            var result = validator.validate(request);

            // Assert
            assertThat(result.size()).isEqualTo(1);
            var errorMessages = result.stream().map(e -> e.getMessage()).toList();
            assertThat(errorMessages).contains("ユーザー名は必須です");
        }

        @Test
        @DisplayName("リマインド時刻が空文字・空白の場合にバリデーションエラーが発生すること")
        void remindTimeIsBlank() {
            // Arrange
            UserSettingRequest request = createValidRequest();
            request.setRemindTime("");

            // Act
            var result = validator.validate(request);

            // Assert
            var errorMessages = result.stream().map(e -> e.getMessage()).toList();
            assertThat(errorMessages).contains("リマインド時刻は必須です");
        }

        @ParameterizedTest
        @ValueSource(strings = {"9:00", "24:00", "12:60", "invalid", "18-30", "18:3"})
        @DisplayName("リマインド時刻が不適切なフォーマットの場合にバリデーションエラーが発生すること")
        void remindTimeInvalidPattern(String invalidTime) {
            // Arrange
            UserSettingRequest request = createValidRequest();
            request.setRemindTime(invalidTime);

            // Act
            var result = validator.validate(request);

            // Assert
            var errorMessages = result.stream().map(e -> e.getMessage()).toList();
            assertThat(errorMessages).contains("リマインド時刻は HH:mm 形式で指定してください（例: 22:00）");
        }

        @Test
        @DisplayName("GoogleカレンダーIDが空文字・空白の場合にバリデーションエラーが発生すること")
        void googleCalendarIdIsBlank() {
            // Arrange
            UserSettingRequest request = createValidRequest();
            request.setGoogleCalendarId("");

            // Act
            var result = validator.validate(request);

            // Assert
            assertThat(result.size()).isEqualTo(1);
            var errorMessages = result.stream().map(e -> e.getMessage()).toList();
            assertThat(errorMessages).contains("Google カレンダー ID は必須です");
        }
    }

    private UserSettingRequest createValidRequest() {
        UserSettingRequest request = new UserSettingRequest();
        request.setUserName("Valid User");
        request.setRemindTime("18:30");
        request.setGoogleCalendarId("primary");
        request.setActive(true);
        request.setGoogleLinked(true);
        return request;
    }
}
