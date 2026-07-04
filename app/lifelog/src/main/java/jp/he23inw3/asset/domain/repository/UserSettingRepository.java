package jp.he23inw3.asset.domain.repository;

import java.util.List;
import java.util.Optional;
import jp.he23inw3.asset.domain.model.UserSetting;

/**
 * ユーザー個別設定情報の永続化および検索を行うためのリポジトリインターフェース。
 */
public interface UserSettingRepository {

    /**
     * ユーザー設定情報を保存または更新します。
     *
     * @param setting 保存対象のユーザー設定ドメインモデル
     */
    void save(UserSetting setting);

    /**
     * 指定された Slack ユーザーIDに対応する設定情報を取得します。
     *
     * @param slackUserId Slack ユーザーID
     * @return ユーザー設定を格納した {@link Optional}。存在しない場合は {@link Optional#empty()}
     */
    Optional<UserSetting> findById(String slackUserId);

    /**
     * リマインド通知が有効（active = true）なすべてのユーザー設定一覧を取得します。
     *
     * @return 有効なユーザー設定のリスト
     */
    List<UserSetting> findAllActive();

    /**
     * すべてのユーザー設定一覧を取得します。
     *
     * @return すべてのユーザー設定のリスト
     */
    List<UserSetting> findAll();

    /**
     * 指定されたメールアドレスに対応するユーザー設定を取得します。
     *
     * @param email OIDC メールアドレス
     * @return ユーザー設定を格納した {@link Optional}。存在しない場合は {@link Optional#empty()}
     */
    Optional<UserSetting> findByEmail(String email);

    /**
     * 指定された Slack ユーザーIDに対応するユーザー設定および認証情報を削除します。
     *
     * @param slackUserId Slack ユーザーID
     */
    void delete(String slackUserId);

    /**
     * 古い Slack ユーザーIDのデータを削除し、新しい設定データをアトミックに保存します。
     *
     * @param oldSlackUserId 削除対象の古い Slack ユーザーID
     * @param newSetting 保存対象の新しいユーザー設定ドメインモデル
     */
    void saveWithMigration(String oldSlackUserId, UserSetting newSetting);
}
