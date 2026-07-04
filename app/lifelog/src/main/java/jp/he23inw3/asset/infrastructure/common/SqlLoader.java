package jp.he23inw3.asset.infrastructure.common;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * クラスパス上の外部 SQL テンプレートファイルを読み込むためのユーティリティクラス。
 * <p>
 * 主に BigQuery 等の SQL クエリをソースコード外で管理するために使用します。
 */
@ApplicationScoped
public class SqlLoader {

    /**
     * 指定されたリソースパスから SQL ファイルの内容を文字列として一括ロードします。
     *
     * @param path クラスパス上の SQL ファイルパス (例: "sql/query.sql")
     * @return ロードされた SQL 文字列
     * @throws IllegalArgumentException 指定されたパスにファイルが存在しない場合
     * @throws RuntimeException ロード時の I/O エラー時
     */
    public static String load(String path) {
        try (InputStream is = SqlLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("SQL file not found at " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load SQL file: " + path, e);
        }
    }
}
