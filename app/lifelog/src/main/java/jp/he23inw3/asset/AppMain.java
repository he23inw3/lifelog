package jp.he23inw3.asset;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.runtime.configuration.ConfigUtils;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;
import jp.he23inw3.asset.batch.BatchExecutionService;
import jp.he23inw3.asset.batch.BatchExecutor;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * アプリケーションのメインエントリーポイントクラス。
 * <p>
 * Quarkus アプリケーションの起動および、プロファイルに応じた起動モード （Web API
 * サーバーモードまたはバッチ処理実行モード）の動的な振り分けを行います。
 */
@Slf4j
@QuarkusMain
public class AppMain {

    /**
     * Quarkus アプリケーションのライフサイクル制御実装クラス。
     */
    public static class LifelogApp implements QuarkusApplication {

        /**
         * アプリケーション実行時の制御フロー。
         *
         * @param args
         *            コマンドライン引数
         * @return 終了コード（0: 正常終了, 1: 異常終了）
         * @throws Exception
         *             実行時の例外
         */
        @Override
        public int run(String... args) throws Exception {
            // 実行プロファイルを取得（デフォルトはapi）
            String activeProfile = ConfigUtils.getProfiles().stream().findFirst().orElse("api");

            // 該当プロファイル名で命名された BatchExecutor Bean が存在するか確認する
            Instance<BatchExecutor> executorInstance = CDI.current()
                    .select(BatchExecutor.class, NamedLiteral.of(activeProfile));

            if (!executorInstance.isUnsatisfied()) {
                try {
                    BatchExecutionService executionService = CDI.current().select(BatchExecutionService.class).get();

                    executionService.executeBatch(activeProfile);
                    return 0; // 正常終了
                } catch (Exception e) {
                    log.error(MessageHelper.getMessage("app.error.batch.notfound", activeProfile), e);
                    return 1; // 異常終了
                }
            }

            // それ以外は通常の常駐Webサーバーとして起動
            log.info(MessageHelper.getMessage("app.startup.api"));
            Quarkus.waitForExit();
            return 0;
        }
    }

    /**
     * アプリケーションの起動メインメソッド。
     *
     * @param args
     *            コマンドライン引数
     */
    public static void main(String... args) {
        Quarkus.run(LifelogApp.class, args);
    }
}
