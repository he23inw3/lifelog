package jp.he23inw3.asset.domain.model;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * システムのヘルスチェック結果を保持するドメインモデルクラス。
 * <p>
 * システム全体の稼働状態 (status) および、各外部連携サービスの個別状態 (components) を管理します。
 */
@Getter
@RequiredArgsConstructor
public class HealthCheckResult {

    /** システム全体のステータス */
    private final HealthStatus status;

    /** 個別サービスのヘルス状態マップ */
    private final Map<String, HealthStatus> components;
}
