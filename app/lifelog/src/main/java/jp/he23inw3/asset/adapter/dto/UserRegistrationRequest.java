package jp.he23inw3.asset.adapter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * ユーザー初回登録 API (POST /api/v1/users/register) リクエスト DTO。
 */
@Data
public class UserRegistrationRequest {

    /** Slack ユーザー ID */
    private String slackUserId;

    /** Slack 連携用一時トークン */
    private String slackToken;

    /** ユーザー名（表示名） */
    @NotBlank(message = "ユーザー名は必須です")
    private String userName;

    /**
     * Slack リマインド送信時刻（HH:mm 形式）。 例: "22:00"
     */
    @NotBlank(message = "リマインド時刻は必須です")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "リマインド時刻は HH:mm 形式で指定してください（例: 22:00）")
    private String remindTime;

    /**
     * 連携する Google カレンダー ID。 例: "example@gmail.com"
     */
    @NotBlank(message = "Google カレンダー ID は必須です")
    private String googleCalendarId;

    /** リマインド対象として有効かどうか（デフォルト: true） */
    private boolean active = true;

    /** Google 連携済みフラグ */
    private boolean googleLinked;
}
