package jp.he23inw3.asset.domain.gateway;

/**
 * Slack API を介して、特定のチャンネルやユーザーへメッセージ送信を行うためのゲートウェイインターフェース。
 */
public interface SlackGateway {

    /**
     * 指定された Slack チャンネルまたはユーザー宛てにテキストメッセージを投稿します。
     *
     * @param channelId メッセージ送信先となる Slack チャンネル ID またはユーザー ID (DM用)
     * @param text 送信するメッセージの本文
     */
    void postMessage(String channelId, String text);

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
    void postConfirmationMessage(String slackUserId, String logDate, String tasks, double workHours, String diary,
            boolean isHoliday);

    /**
     * Slack の responseUrl 宛てに指定されたテキストでメッセージを更新（置換）します。
     *
     * @param responseUrl Slackインタラクションイベントから取得した返信用URL
     * @param text 更新後のメッセージ本文
     */
    void updateMessage(String responseUrl, String text);
}
