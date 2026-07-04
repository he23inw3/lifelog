package jp.he23inw3.asset.domain.gateway;

/**
 * 機微情報を暗号化・復号するためのゲートウェイインターフェース。
 */
public interface CryptoGateway {

    /**
     * 平文を暗号化して Base64 文字列を返します。
     *
     * @param plainText 暗号化対象の平文
     * @return Base64 エンコードされた暗号文
     */
    String encrypt(String plainText);

    /**
     * 暗号化された Base64 文字列を復号して平文を返します。
     *
     * @param cipherText 暗号化された Base64 文字列
     * @return 復号された平文
     */
    String decrypt(String cipherText);
}
