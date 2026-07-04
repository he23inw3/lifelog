package jp.he23inw3.asset.adapter.rest.interaction;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Slack Block Kit 等のアクションインタラクションを処理するハンドラーのインターフェース。
 */
public interface SlackInteractionHandler {

    /**
     * 指定されたインタラクションがこのハンドラーで処理可能かどうかを判定します。
     *
     * @param type インタラクションタイプ (例: "block_actions")
     * @param actionId アクションID
     * @return 処理可能な場合は true、それ以外は false
     */
    boolean canHandle(String type, String actionId);

    /**
     * インタラクションの処理を実行します。
     *
     * @param root インタラクションのペイロードデータのルートノード
     */
    void handle(JsonNode root);
}
