package jp.he23inw3.asset.batch;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import jp.he23inw3.asset.domain.model.BatchExecutionHistory;
import jp.he23inw3.asset.domain.model.BatchStatus;
import jp.he23inw3.asset.domain.repository.BatchExecutionHistoryRepository;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import jp.he23inw3.asset.infrastructure.common.TraceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * バッチプロセスの実行およびそのライフサイクル（履歴管理・ログ出力）を制御する共通サービス。
 * <p>
 * 個別バッチの起動ログ出力、Firestore への実行履歴（起動時間、成否、スタックトレース等）の記録を行います。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class BatchExecutionService {

    private final BatchExecutionHistoryRepository historyRepository;

    /**
     * 指定されたプロファイル名に対応するバッチを実行し、その履歴を永続化します。
     *
     * @param batchName
     *            実行対象のバッチ名 (Quarkus プロファイル名)
     * @throws Exception
     *             バッチ実行中に発生した例外
     */
    public void executeBatch(String batchName) throws Exception {
        BatchExecutionHistory history = null;
        try {
            String historyId = UUID.randomUUID().toString();
            history = BatchExecutionHistory.builder()
                    .id(historyId)
                    .batchName(batchName)
                    .startedAt(DateTimeUtil.nowLocalDateTime())
                    .status(BatchStatus.RUNNING)
                    .traceId(TraceHelper.currentTraceId())
                    .build();

            historyRepository.save(history);
        } catch (Exception e) {
            log.error(MessageHelper.getMessage("app.error.batch.history.start"), e);
        }

        try {
            BatchExecutor executor = CDI.current().select(BatchExecutor.class, NamedLiteral.of(batchName)).get();

            log.info(MessageHelper.getMessage("app.startup.batch", batchName));
            executor.execute();

            if (history != null) {
                history.setFinishedAt(DateTimeUtil.nowLocalDateTime());
                history.setStatus(BatchStatus.SUCCESS);
                historyRepository.save(history);
            }
            log.info(MessageHelper.getMessage("app.shutdown.batch", batchName));
        } catch (Exception e) {
            if (history != null) {
                history.setFinishedAt(DateTimeUtil.nowLocalDateTime());
                history.setStatus(BatchStatus.FAILED);
                history.setErrorMessage(e.getMessage());

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                history.setErrorStackTrace(sw.toString());

                try {
                    historyRepository.save(history);
                } catch (Exception ex) {
                    log.error(MessageHelper.getMessage("app.error.batch.history.failure"), ex);
                }
            }
            throw e;
        }
    }
}
