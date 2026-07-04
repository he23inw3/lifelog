package jp.he23inw3.asset.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Slack アカウント連携成功時のレスポンス DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlackLinkageResponse {

    /** 連携結果メッセージ */
    private String message;
}
