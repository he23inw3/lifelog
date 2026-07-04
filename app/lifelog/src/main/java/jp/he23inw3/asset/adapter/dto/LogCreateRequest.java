package jp.he23inw3.asset.adapter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 日報登録 API (POST /api/v1/users/me/logs) リクエスト DTO。
 */
@Getter
@Setter
public class LogCreateRequest {

    /**
     * 日報・日記の未加工テキスト。 「本日の作業: ..., 日記: ...」形式など自由記述。
     */
    @NotBlank(message = "日報テキストは必須です")
    private String rawText;

    /** 休暇フラグ（true の場合は休暇日として記録） */
    private boolean holiday = false;
}
