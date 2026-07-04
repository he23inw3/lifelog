package jp.he23inw3.asset.infrastructure.demo;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.gateway.SlackGateway;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import jp.he23inw3.asset.infrastructure.slack.SlackGatewayImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * デモ/本番を透過的に切り替える {@link SlackGateway} ルータークラス。
 * <p>
 * {@code @Alternative @Priority(1)} により、このクラスが CDI の注入候補として優先されます。
 * {@code lifelog.demo.enabled} が {@code true} の場合は
 * {@link DemoSlackGatewayImpl}、 それ以外は {@link SlackGatewayImpl} に委譲します。
 */
@Slf4j
@Alternative
@Priority(1)
@ApplicationScoped
@RequiredArgsConstructor
public class SlackGatewayRouter implements SlackGateway {

    private final LifeLogConfig config;

    private final SlackGatewayImpl realGateway;

    private final DemoSlackGatewayImpl demoGateway;

    /**
     * 指定された Slack チャンネルまたはユーザー宛てにテキストメッセージを投稿します。
     *
     * @param channelId メッセージ送信先となる Slack チャンネル ID またはユーザー ID (DM用)
     * @param text 送信するメッセージの本文
     */
    @Override
    public void postMessage(String channelId, String text) {
        log.debug(MessageHelper.getMessage("infra.slack.router.debug", config.demo().enabled(), channelId));
        getTarget().postMessage(channelId, text);
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
        getTarget().postConfirmationMessage(slackUserId, logDate, tasks, workHours, diary, isHoliday);
    }

    /**
     * Slack の responseUrl 宛てに指定されたテキストでメッセージを更新（置換）します。
     *
     * @param responseUrl Slackインタラクションイベントから取得した返信用URL
     * @param text 更新後のメッセージ本文
     */
    @Override
    public void updateMessage(String responseUrl, String text) {
        getTarget().updateMessage(responseUrl, text);
    }

    private SlackGateway getTarget() {
        return config.demo().enabled() ? demoGateway : realGateway;
    }
}
