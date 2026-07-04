package jp.he23inw3.asset.adapter.rest.event;

import java.util.Map;

/**
 * Slack Events API から送信される各種イベントを処理するハンドラーのインターフェース。
 */
public interface SlackEventHandler {

    /**
     * 指定されたイベントがこのハンドラーで処理可能かどうかを判定します。
     *
     * @param eventType イベントタイプ (例: "message")
     * @param eventData イベントの詳細データ
     * @return 処理可能な場合は true、それ以外は false
     */
    boolean canHandle(String eventType, Map<String, Object> eventData);

    /**
     * イベントの処理を実行します。
     *
     * @param eventData イベントの詳細データ
     */
    void handle(Map<String, Object> eventData);
}
