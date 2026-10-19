package jp.he23inw3.asset.domain.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

/**
 * ユーザー個別設定情報を保持するドメインモデル（値オブジェクト）。
 * <p>
 * Slack ユーザー ID、表示用のユーザー名、日報リマインド時刻、Google カレンダー ID、
 * およびリマインド機能の有効・無効状態などを管理します。
 */
@Value
@Builder(toBuilder = true)
public class UserSetting {

    /** Slack ユーザID */
    private String slackUserId;

    /** OIDC メールアドレス（email ↔ slackUserId のマッピングキー） */
    private String email;

    /** 表示名 */
    private String userName;

    /** リマインド時刻 */
    private String remindTime;

    /** Google Calendar ID */
    private String googleCalendarId;

    /** アクティブフラグ */
    private boolean active;

    /** Google 連携済みフラグ */
    private boolean googleLinked;

    /** 暗号化された Google OAuth リフレッシュトークン */
    private String googleRefreshToken;

    /** 作成日時 */
    private Instant createdAt;

    /** 更新日時 */
    private Instant updatedAt;
}
