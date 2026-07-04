package jp.he23inw3.asset.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * 管理ダッシュボードの統計情報を表すドメインモデル（値オブジェクト）。
 */
@Value
@Builder(toBuilder = true)
public class DashboardStats {

    /** 有効ユーザー数 */
    int activeUserCount;

    /** 本日の登録ログ件数 */
    int todayLogCount;

    /** 有効なセッション数 */
    int activeSessionCount;

    /** 本日のバッチエラー件数 */
    int todayBatchErrorCount;
}
