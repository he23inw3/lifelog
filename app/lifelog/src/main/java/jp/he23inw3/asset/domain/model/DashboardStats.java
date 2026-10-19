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
    private int activeUserCount;

    /** 本日の登録ログ件数 */
    private int todayLogCount;

    /** 有効なセッション数 */
    private int activeSessionCount;

    /** 本日のバッチエラー件数 */
    private int todayBatchErrorCount;
}
