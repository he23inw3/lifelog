package jp.he23inw3.asset.domain.repository;

import java.util.Optional;
import jp.he23inw3.asset.domain.model.SlackLinkageToken;

/**
 * Slack アカウント連携用一時トークン情報の永続化および検索を行うためのリポジトリインターフェース。
 */
public interface SlackLinkageTokenRepository {

    /**
     * 一時トークン情報を保存します。
     *
     * @param linkageToken 保存対象のトークン情報
     */
    void save(SlackLinkageToken linkageToken);

    /**
     * 指定されたトークンに対応する情報を取得します。
     *
     * @param token 検索対象のトークン文字列
     * @return トークン情報を格納した {@link Optional}。存在しない場合は {@link Optional#empty()}
     */
    Optional<SlackLinkageToken> findByToken(String token);

    /**
     * 指定されたトークン情報を削除します。
     *
     * @param token 削除対象のトークン文字列
     */
    void delete(String token);
}
