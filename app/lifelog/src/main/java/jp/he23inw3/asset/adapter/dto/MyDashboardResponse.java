package jp.he23inw3.asset.adapter.dto;

import lombok.Data;

/**
 * マイダッシュボード取得 API (GET /api/v1/users/me/dashboard) レスポンス DTO。
 */
@Data
public class MyDashboardResponse {

    /** 今月の日報登録件数 */
    private int monthlyLogCount;

    /** 今月の稼働合計時間 */
    private double monthlyWorkHours;

    /** 今月の残業合計時間 */
    private double monthlyOvertimeHours;

    /** 最終日報登録日 */
    private String lastLogDate;
}
