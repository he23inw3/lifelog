package jp.he23inw3.asset.adapter.rest.command;

import jakarta.ws.rs.core.Response;

/**
 * Slack Slash Command を個別に処理するハンドラーのインターフェース。
 */
public interface SlackCommandHandler {

    /**
     * 指定されたコマンド名がこのハンドラーで処理可能かどうかを判定します。
     *
     * @param command コマンド名 (例: "/lifelog-link")
     * @return 処理可能な場合は true、それ以外は false
     */
    boolean canHandle(String command);

    /**
     * コマンドの処理を実行します。
     *
     * @param command コマンド名
     * @param userId Slack ユーザーID
     * @param userName Slack ユーザー名
     * @return Slack へのレスポンス
     */
    Response handle(String command, String userId, String userName);
}
