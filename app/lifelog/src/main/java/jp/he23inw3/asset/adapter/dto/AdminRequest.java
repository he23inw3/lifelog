package jp.he23inw3.asset.adapter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理者情報の登録・更新 API (PUT /api/v1/admins/{email}) リクエスト DTO。
 */
@Data
public class AdminRequest {

    /** 管理者の表示用ユーザー名 */
    @NotBlank(message = "管理者名は必須です")
    private String userName;

    /** 管理者アカウントが有効かどうか（デフォルト: true） */
    private boolean active = true;
}
