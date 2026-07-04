package jp.he23inw3.asset.batch;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jp.he23inw3.asset.usecase.MonthlyReflectionUseCase;
import lombok.RequiredArgsConstructor;

/**
 * 月末 AI パーソナル振り返りレポートバッチ実行クラス。
 * <p>
 * Cloud Run Jobs から {@code QUARKUS_PROFILE=reflection-batch} で起動される。
 *  Cron: {@code 0 23 28-31 * *}（毎月末日候補の23:00）「本日が月末日か」を判定し、末日以外はスキップする。
 */
@Named("reflection-batch")
@ApplicationScoped
@RequiredArgsConstructor
public class MonthlyReflectionExecutor extends BatchExecutor {

    private final MonthlyReflectionUseCase useCase;

    @Override
    public void invoke() throws Exception {
        useCase.createMonthlyReport();
    }

    @Override
    public String getBatchName() {
        return "BE-BATCH002(MonthlyReflection)";
    }
}
