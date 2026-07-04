package jp.he23inw3.asset.adapter.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 管理者情報取得・更新 API (GET/PUT /api/v1/admins/{email}) レスポンス DTO。
 */
@Data
@Builder
public class AdminResponse {

    /** 管理者ユーザーの表示名 */
    private String userName;

    /** 管理者アカウントが現在有効かどうかのステータス */
    private boolean active;

    /** 管理者が作成された日時 */
    private LocalDateTime createdAt;

    /** 管理者が最後に更新された日時 */
    private LocalDateTime updatedAt;
}
