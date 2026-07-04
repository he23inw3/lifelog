package jp.he23inw3.asset.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理者向けカレンダー再同期結果のレスポンス DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogCalendarSyncResponse {

    /** 成功メッセージ */
    private String message;
}
