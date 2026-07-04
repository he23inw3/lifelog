package jp.he23inw3.asset.batch;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jp.he23inw3.asset.usecase.RemindCheckUseCase;
import lombok.RequiredArgsConstructor;

/**
 * 毎時リマインドバッチ実行クラス。
 * <p>
 * Cloud Run Jobs から {@code QUARKUS_PROFILE=remind-batch} で起動される。 Cron:
 * {@code 0 * * * *}（毎時0分）
 */
@Named("remind-batch")
@ApplicationScoped
@RequiredArgsConstructor
public class RemindCheckExecutor extends BatchExecutor {

    private final RemindCheckUseCase useCase;

    @Override
    public void invoke() throws Exception {
        useCase.checkAndSendRemind();
    }

    @Override
    public String getBatchName() {
        return "BE-BATCH001(RemindCheck)";
    }
}
