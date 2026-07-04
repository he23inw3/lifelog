package jp.he23inw3.asset.adapter.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * ユーザー設定取得 API (GET /api/v1/users/{slackUserId}/settings) レスポンス DTO。
 */
@Data
@Builder
public class UserSettingResponse {

    /** Slack ユーザー ID */
    private String slackUserId;

    /** OIDC メールアドレス */
    private String email;

    /** ユーザー名 */
    private String userName;

    /** Slack リマインド送信時刻（HH:mm 形式） */
    private String remindTime;

    /** 連携する Google カレンダー ID */
    private String googleCalendarId;

    /** リマインド有効フラグ */
    private boolean active;

    /** Google 連携済みフラグ */
    private boolean googleLinked;

    /** 設定の初回登録日時 */
    private LocalDateTime createdAt;
}
