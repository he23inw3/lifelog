package jp.he23inw3.asset.adapter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Slack アカウント連携リクエスト DTO。
 */
@Data
public class SlackLinkageRequest {
    /** 連携用の一時トークン */
    @NotBlank(message = "Token is required")
    private String token;
}
