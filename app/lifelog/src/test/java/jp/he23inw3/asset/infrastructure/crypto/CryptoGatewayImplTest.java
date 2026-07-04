package jp.he23inw3.asset.infrastructure.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Base64;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.exception.CryptoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CryptoGatewayImplTest {

    @InjectMocks
    CryptoGatewayImpl cryptoGateway;

    @Mock
    LifeLogConfig config;

    @Nested
    @DisplayName("暗号化と復号")
    class EncryptAndDecrypt {

        @Test
        @DisplayName("暗号化したデータを正しく復号できること")
        void testEncryptDecrypt() {
            LifeLogConfig.Crypto cryptoMock = mock(LifeLogConfig.Crypto.class);
            when(cryptoMock.keyValue()).thenReturn("1234567890123456");
            when(config.crypto()).thenReturn(cryptoMock);

            // Manually invoke init() to simulate the CDI @PostConstruct hook
            cryptoGateway.init();

            String original = "dummy-refresh-token-value";
            String encrypted = cryptoGateway.encrypt(original);

            String decrypted = cryptoGateway.decrypt(encrypted);
            assertThat(decrypted).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("復号エラー")
    class DecryptFailure {

        @Test
        @DisplayName("不正なBase64形式のデータを復号した場合はCryptoExceptionがスローされること")
        void testDecryptInvalidDataThrowsCryptoException() {
            assertThatThrownBy(() -> cryptoGateway.decrypt("invalid-base64")).isInstanceOf(CryptoException.class)
                    .hasMessageContaining("Decryption failed");
        }

        @Test
        @DisplayName("データ長が極端に短いデータを復号した場合はCryptoExceptionがスローされること")
        void testDecryptShortDataThrowsCryptoException() {
            String shortBase64 = Base64.getEncoder().encodeToString("short".getBytes());
            assertThatThrownBy(() -> cryptoGateway.decrypt(shortBase64)).isInstanceOf(CryptoException.class)
                    .hasMessageContaining("Decryption failed: Invalid cipher text length");
        }
    }
}
