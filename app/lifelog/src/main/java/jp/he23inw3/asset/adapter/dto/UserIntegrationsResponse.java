package jp.he23inw3.asset.adapter.dto;

import lombok.Builder;
import lombok.Data;

/**
 * ログインユーザーのアカウント外部連携状態を示すレスポンス DTO。
 */
@Data
@Builder
public class UserIntegrationsResponse {

    /** Google 連携済みフラグ */
    private boolean googleLinked;

    /** Google カレンダー ID */
    private String googleCalendarId;

    /** Slack 連携済みフラグ */
    private boolean slackLinked;

    /** Slack ユーザー ID */
    private String slackUserId;
}
