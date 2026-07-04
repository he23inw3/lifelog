package jp.he23inw3.asset.infrastructure.demo;

import com.google.cloud.firestore.Firestore;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import jp.he23inw3.asset.domain.constant.FirestoreCollectionNames;
import jp.he23inw3.asset.domain.exception.ExternalServiceException;
import jp.he23inw3.asset.domain.gateway.SlackGateway;
import jp.he23inw3.asset.domain.util.InstantUtil;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * デモモード用の擬似 Slack ゲートウェイ実装。
 * <p>
 * Slack API の代わりに Firestore コレクション {@code demo_slack_messages} にメッセージを永続化します。
 * フロントエンドは {@code GET /api/v1/demo/messages} でこれらのメッセージを取得できます。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DemoSlackGatewayImpl implements SlackGateway {

    private final Firestore firestore;

    /**
     * 指定された Slack チャンネルまたはユーザー宛てにテキストメッセージを投稿します。
     *
     * @param channelId メッセージ送信先となる Slack チャンネル ID またはユーザー ID (DM用)
     * @param text 送信するメッセージの本文
     */
    @Override
    public void postMessage(String channelId, String text) {
        saveMessage(channelId, "POST", text);
        log.info(MessageHelper.getMessage("infra.demo.slack.post", channelId, text));
    }

    /**
     * ユーザーの日報の確定前確認メッセージ（Block Kit）を ephemeral で投稿します。
     *
     * @param slackUserId 送信先の Slack ユーザーID
     * @param logDate 日報日付
     * @param tasks 業務内容
     * @param workHours 稼働時間
     * @param diary 日記内容
     * @param isHoliday 休暇フラグ
     */
    @Override
    public void postConfirmationMessage(String slackUserId, String logDate, String tasks, double workHours,
            String diary, boolean isHoliday) {
        String text = String.format("【確認】%s / %s / 稼働: %.1fh / 日記: %s / 休暇: %s", logDate, tasks, workHours, diary,
                isHoliday ? "はい" : "いいえ");
        saveMessage(slackUserId, "CONFIRM", text);
        log.info(MessageHelper.getMessage("infra.demo.slack.confirm", slackUserId, text));
    }

    /**
     * Slack の responseUrl 宛てに指定されたテキストでメッセージを更新（置換）します。
     *
     * @param responseUrl Slackインタラクションイベントから取得した返信用URL
     * @param text 更新後のメッセージ本文
     */
    @Override
    public void updateMessage(String responseUrl, String text) {
        saveMessage("RESPONSE_URL", "UPDATE", text);
        log.info(MessageHelper.getMessage("infra.demo.slack.update", text));
    }

    private void saveMessage(String slackUserId, String type, String text) {
        try {
            String collectionName = FirestoreCollectionNames.DEMO_SLACK_MESSAGES;
            Map<String, Object> data = new HashMap<>();
            data.put("slackUserId", slackUserId);
            data.put("type", type);
            data.put("text", text);
            data.put("timestamp", InstantUtil.nowEpochSecond());
            firestore.collection(collectionName).add(data).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("デモ Slack メッセージの保存に失敗しました。", e);
        }
    }
}
