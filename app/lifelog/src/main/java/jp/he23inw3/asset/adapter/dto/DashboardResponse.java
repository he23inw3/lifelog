package jp.he23inw3.asset.adapter.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 管理ダッシュボード情報取得 API (GET /api/v1/admin/dashboard) レスポンス DTO。
 */
@Data
@Builder
public class DashboardResponse {

    /** 有効ユーザー数 */
    private int activeUserCount;

    /** 本日の登録ログ件数 */
    private int todayLogCount;

    /** 有効なセッション数 */
    private int activeSessionCount;

    /** 本日のバッチエラー件数 */
    private int todayBatchErrorCount;
}
