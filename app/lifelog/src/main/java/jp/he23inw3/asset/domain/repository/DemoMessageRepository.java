package jp.he23inw3.asset.domain.repository;

import java.util.List;
import jp.he23inw3.asset.domain.model.DemoMessage;

/**
 * デモ用の Slack メッセージを管理するリポジトリインターフェース。
 */
public interface DemoMessageRepository {

    /**
     * 指定された Slack ユーザーIDに該当するデモ用メッセージ一覧を取得します。
     *
     * @param slackUserId Slack ユーザーID
     * @return デモ用メッセージのリスト
     */
    List<DemoMessage> findBySlackUserId(String slackUserId);
}
