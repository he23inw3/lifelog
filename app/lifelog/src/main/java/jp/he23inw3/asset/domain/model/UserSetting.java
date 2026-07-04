package jp.he23inw3.asset.domain.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

/**
 * ユーザー個別設定情報を保持するドメインモデル（値オブジェクト）。
 * <p>
 * Slack ユーザー ID、表示用のユーザー名、日報リマインド時刻、Google カレンダー ID、 およびリマインド機能の有効・無効状態などを管理します。
 */
@Value
@Builder(toBuilder = true)
public class UserSetting {

    /** Slack ユーザID */
    String slackUserId;

    /** OIDC メールアドレス（email ↔ slackUserId のマッピングキー） */
    String email;

    /** 表示名 */
    String userName;

    /** リマインド時刻 */
    String remindTime;

    /** Google Calendar ID */
    String googleCalendarId;

    /** アクティブフラグ */
    boolean active;

    /** Google 連携済みフラグ */
    boolean googleLinked;

    /** 暗号化された Google OAuth リフレッシュトークン */
    String googleRefreshToken;

    /** 作成日時 */
    Instant createdAt;

    /** 更新日時 */
    Instant updatedAt;
}
