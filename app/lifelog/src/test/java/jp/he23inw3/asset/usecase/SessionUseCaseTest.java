package jp.he23inw3.asset.usecase;

import static org.mockito.Mockito.verify;

import jp.he23inw3.asset.domain.repository.UserSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionUseCaseTest {

    @Mock
    UserSessionRepository userSessionRepository;

    @InjectMocks
    SessionUseCase target;

    @Nested
    @DisplayName("対話セッション強制リセット")
    class ResetSession {

        @Test
        @DisplayName("指定ユーザーの対話セッションを強制リセットすること")
        void resetSession_Success() {
            // Arrange
            String slackUserId = "U123456";

            // Act
            target.resetSession(slackUserId);

            // Assert
            verify(userSessionRepository).delete(slackUserId);
        }
    }
}
