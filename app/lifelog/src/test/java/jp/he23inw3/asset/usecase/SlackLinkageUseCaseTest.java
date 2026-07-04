package jp.he23inw3.asset.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.exception.InvalidTokenException;
import jp.he23inw3.asset.domain.model.SlackLinkageToken;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.SlackLinkageTokenRepository;
import jp.he23inw3.asset.domain.service.UserSettingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SlackLinkageUseCaseTest {

    @Mock
    LifeLogConfig config;

    @Mock
    LifeLogConfig.Portal configPortal;

    @Mock
    UserSettingService userSettingService;

    @Mock
    SlackLinkageTokenRepository linkageTokenRepository;

    @InjectMocks
    SlackLinkageUseCase target;

    @Nested
    @DisplayName("連携用トークンURLの生成")
    class GenerateLinkageUrl {

        @Test
        @DisplayName("連携用トークンを正しく生成しURLを返却できること")
        void generateLinkageUrl_Success() throws Exception {
            when(config.portal()).thenReturn(configPortal);
            when(configPortal.baseUrl()).thenReturn("http://localhost:5173");

            String url = target.generateLinkageUrl("U123456");

            assertThat(url).contains("http://localhost:5173/settings?slackToken=");
            verify(linkageTokenRepository).save(any(SlackLinkageToken.class));
        }
    }

    @Nested
    @DisplayName("Slackアカウント連携実行")
    class LinkSlack {

        @Test
        @DisplayName("有効なトークンを用いてSlackアカウント連携が正常に実行できること")
        void linkSlack_Success() throws Exception {
            String token = "valid-token";
            String email = "user@example.com";
            String slackUserId = "U123456";

            SlackLinkageToken linkageToken = SlackLinkageToken.builder()
                    .token(token)
                    .slackUserId(slackUserId)
                    .expiresAt(Instant.now().plus(600, ChronoUnit.SECONDS))
                    .build();

            when(linkageTokenRepository.findByToken(token)).thenReturn(Optional.of(linkageToken));

            UserSetting mockSetting = UserSetting.builder().slackUserId(slackUserId).email(email).userName("user").build();
            when(userSettingService.linkSlackAccount(email, slackUserId)).thenReturn(mockSetting);

            target.linkSlack(email, token);

            verify(linkageTokenRepository).delete(token);
            verify(userSettingService).linkSlackAccount(email, slackUserId);
        }

        @Test
        @DisplayName("無効なトークンまたは期限切れトークンの場合、例外をスローすること")
        void linkSlack_InvalidToken_ThrowsException() throws Exception {
            String token = "invalid-token";
            String email = "user@example.com";

            when(linkageTokenRepository.findByToken(token)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> target.linkSlack(email, token)).isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("Invalid or expired token");
        }
    }
}
