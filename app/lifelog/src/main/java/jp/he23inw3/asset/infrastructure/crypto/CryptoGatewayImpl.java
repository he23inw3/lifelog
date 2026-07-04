package jp.he23inw3.asset.infrastructure.crypto;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.exception.CryptoException;
import jp.he23inw3.asset.domain.gateway.CryptoGateway;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 機微情報を AES-256-GCM で暗号化・復号するゲートウェイの実装。 
 * 96bit IVを毎回生成し、暗号文の先頭に付与して Base64 エンコードした形式で出力する。 
 * 暗号化に使用する鍵は、LifeLogConfig 経由で取得する。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class CryptoGatewayImpl implements CryptoGateway {

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final int MIN_CIPHER_LENGTH = GCM_IV_LENGTH + GCM_TAG_LENGTH;

    private final LifeLogConfig config;

    private final SecureRandom secureRandom = new SecureRandom();

    private SecretKeySpec secretKeySpec;

    /**
     * 平文を暗号化して Base64 文字列を返します。
     *
     * @param plainText 暗号化対象の平文
     * @return Base64 エンコードされた暗号文
     */
    @Override
    public String encrypt(String plainText) {
        if (StringUtils.isBlank(plainText)) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = new byte[GCM_IV_LENGTH]; // GCM 推奨 nonce 長 (12 bytes)
            secureRandom.nextBytes(iv);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

            cipher.init(Cipher.ENCRYPT_MODE, this.secretKeySpec, gcmSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error(MessageHelper.getMessage("infra.crypto.encrypt.error"), e);
            throw new CryptoException("Encryption failed", e);
        }
    }

    /**
     * 暗号化された Base64 文字列を復号して平文を返します。
     *
     * @param cipherText 暗号化された Base64 文字列
     * @return 復号された平文
     */
    @Override
    public String decrypt(String cipherText) {
        if (StringUtils.isBlank(cipherText)) {
            return null;
        }

        try {
            byte[] combined = Base64.getDecoder().decode(cipherText);
            if (combined.length < MIN_CIPHER_LENGTH) {
                throw new CryptoException("Decryption failed: Invalid cipher text length");
            }

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            byte[] iv = Arrays.copyOfRange(combined, 0, GCM_IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(combined, GCM_IV_LENGTH, combined.length);

            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, this.secretKeySpec, gcmSpec);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            log.error(MessageHelper.getMessage("infra.crypto.decrypt.error"), e);
            throw new CryptoException("Decryption failed", e);
        }
    }

    @PostConstruct
    void init() {
        try {
            String rawKey = config.crypto().keyValue();
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            this.secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            log.error(MessageHelper.getMessage("infra.crypto.initialize.error"), e);
            throw new CryptoException("Initialization failed", e);
        }
    }
}
