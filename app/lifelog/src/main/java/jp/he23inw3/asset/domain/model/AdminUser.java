package jp.he23inw3.asset.domain.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

/**
 * システムの操作権限を持つ管理者ユーザー情報を保持するドメインモデル（値オブジェクト）。
 * <p>
 * OIDC トークンから抽出されたメールアドレスをキー（ドキュメント ID）として識別し、 有効フラグや登録・更新に関する監査情報を管理します。
 */
@Value
@Builder(toBuilder = true)
public class AdminUser {

    /** Google OIDC 認証に基づく管理者のメールアドレス */
    String email;

    /** 管理者ユーザーの表示名 */
    String userName;

    /** 管理者が現在有効かどうか（無効化されている場合は API 実行を制限する） */
    boolean active;

    /** この管理者を作成したオペレーターのメールアドレス */
    String createdBy;

    /** 管理者が作成された日時 */
    Instant createdAt;

    /** この管理者を最後に更新したオペレーターのメールアドレス */
    String updatedBy;

    /** 管理者が最後に更新された日時 */
    Instant updatedAt;
}
