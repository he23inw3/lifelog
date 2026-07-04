package jp.he23inw3.asset.domain.repository;

import java.util.Optional;
import jp.he23inw3.asset.domain.model.AdminUser;

/**
 * 管理者ユーザー情報の永続化および検索を行うためのリポジトリインターフェース。
 */
public interface AdminUserRepository {

    /**
     * 指定されたメールアドレスに対応する管理者ユーザー情報を取得します。
     *
     * @param email 管理者のメールアドレス
     * @return 管理者情報を格納した {@link Optional}。存在しない場合は {@link Optional#empty()}
     */
    Optional<AdminUser> findByEmail(String email);

    /**
     * 管理者ユーザー情報を保存または更新します。
     *
     * @param adminUser 保存対象の管理者ユーザー設定ドメインモデル
     */
    void save(AdminUser adminUser);

    /**
     * 管理者ユーザーが1件も登録されていない（コレクションが空である）かを判定します。
     *
     * @return 登録済みの管理者が存在しない場合は {@code true}、1件以上存在する場合は {@code false}
     */
    boolean isEmpty();
}
