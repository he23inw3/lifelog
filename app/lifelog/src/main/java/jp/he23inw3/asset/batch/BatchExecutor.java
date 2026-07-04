package jp.he23inw3.asset.batch;

import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * バッチ実行インターフェース。
 * <p>
 * 各バッチクラスはこのインターフェースを実装し、{@code @Named} に Quarkus プロファイル名と同じ値を指定することで AppMain
 * から動的に検索される。
 */
@Slf4j
public abstract class BatchExecutor {

    /**
     * バッチ実行メソッド。
     * 
     * @throws Exception
     *             処理中に発生した例外
     */
    public void execute() throws Exception {
        log.info(MessageHelper.getMessage("batch.start", getBatchName()));
        try {
            invoke();
            log.info(MessageHelper.getMessage("batch.success", getBatchName()));
        } catch (Exception e) {
            log.error(MessageHelper.getMessage("batch.failure", getBatchName()), e);
            throw e;
        }
    }

    /**
     * バッチのメイン処理を実行する。
     *
     * @throws Exception
     *             処理中に発生した例外
     */
    public abstract void invoke() throws Exception;

    /**
     * バッチ名を取得する。
     *
     * @return バッチ名
     */
    public abstract String getBatchName();
}
