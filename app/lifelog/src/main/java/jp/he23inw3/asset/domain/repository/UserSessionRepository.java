package jp.he23inw3.asset.domain.repository;

import java.util.List;
import java.util.Optional;
import jp.he23inw3.asset.domain.model.Session;

/**
 * ユーザーの対話セッション情報の永続化および検索を行うためのリポジトリインターフェース。
 */
public interface UserSessionRepository {

    /**
     * 対話セッション情報を保存または更新します。
     *
     * @param session 保存対象の対話セッションドメインモデル
     */
    void save(Session session);

    /**
     * 指定された Slack ユーザーIDに対応する対話セッションを取得します。
     *
     * @param slackUserId Slack ユーザーID
     * @return 対話セッションを格納した {@link Optional}。存在しない場合は {@link Optional#empty()}
     */
    Optional<Session> findById(String slackUserId);

    /**
     * 指定された Slack ユーザーIDに対応する対話セッションを削除します。
     *
     * @param slackUserId Slack ユーザーID
     */
    void delete(String slackUserId);

    /**
     * すべての対話セッション一覧を取得します。
     *
     * @return 対話セッションのリスト
     */
    List<Session> findAll();
}
