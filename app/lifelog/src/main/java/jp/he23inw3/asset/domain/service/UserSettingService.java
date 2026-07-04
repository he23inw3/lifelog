package jp.he23inw3.asset.domain.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import jp.he23inw3.asset.domain.exception.AlreadyExistsException;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.gateway.CryptoGateway;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import jp.he23inw3.asset.domain.util.EmailUtil;
import jp.he23inw3.asset.domain.util.InstantUtil;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * ユーザー個別設定に関連する複雑なドメイン知識・ビジネスルール（アカウント連携等）を扱うドメインサービス。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class UserSettingService {

    private final UserSettingRepository settingRepository;

    private final CryptoGateway cryptoGateway;

    /**
     * 指定されたメールアドレスに Slack ユーザーIDを紐付けます（設定の移行・統合を含みます）。
     *
     * @param email 連携対象のメールアドレス
     * @param slackUserId 紐付ける Slack ユーザーID
     * @return 連携完了後の {@link UserSetting} ドメインモデル
     */
    public UserSetting linkSlackAccount(String email, String slackUserId) {
        Optional<UserSetting> settingByEmail = settingRepository.findByEmail(email);

        if (settingByEmail.isPresent()) {
            UserSetting setting = settingByEmail.get();
            // 既に別の Slack ID が紐づいている場合は、移行のために古い Slack ID のデータを削除して新しい ID で保存（アトミックに実行）
            if (!setting.getSlackUserId().equals(slackUserId)) {
                log.info(MessageHelper.getMessage("service.usersetting.migration", email, setting.getSlackUserId(),
                        slackUserId));
                String oldSlackUserId = setting.getSlackUserId();
                UserSetting migratedSetting = setting.toBuilder()
                        .slackUserId(slackUserId)
                        .updatedAt(InstantUtil.now())
                        .build();
                settingRepository.saveWithMigration(oldSlackUserId, migratedSetting);
                return migratedSetting;
            }
            // 同じ Slack ID の場合はそのまま保存
            settingRepository.save(setting);
            return setting;
        }

        // メールアドレスで設定が見つからなかった場合、新しい Slack ID での既存設定があるか検索
        Optional<UserSetting> settingById = settingRepository.findById(slackUserId);
        if (settingById.isPresent()) {
            UserSetting setting = settingById.get()
                    .toBuilder()
                    .email(email)
                    .updatedAt(InstantUtil.now())
                    .build();
            settingRepository.save(setting);
            return setting;
        }

        // 完全新規ユーザー
        UserSetting newUserSetting = UserSetting.builder()
                .slackUserId(slackUserId)
                .email(email)
                .userName(EmailUtil.extractUserName(email))
                .remindTime("18:00")
                .googleCalendarId(email)
                .active(true)
                .googleLinked(false)
                .createdAt(InstantUtil.now())
                .updatedAt(InstantUtil.now())
                .build();
        settingRepository.save(newUserSetting);
        return newUserSetting;
    }

    /**
     * 指定されたメールアドレスのユーザー設定に Google カレンダーの認証情報を紐付けます。
     *
     * @param email ユーザーのメールアドレス
     * @param refreshToken Google OAuth リフレッシュトークン（暗号化前。null の場合は既存のトークンを再利用）
     */
    public void linkGoogleAccount(String email, String refreshToken) {
        UserSetting existing = settingRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User settings not found for email: " + email));

        UserSetting.UserSettingBuilder builder = existing.toBuilder()
                .googleLinked(true)
                .updatedAt(InstantUtil.now());

        if (StringUtils.isNotBlank(refreshToken)) {
            String encryptedToken = cryptoGateway.encrypt(refreshToken);
            builder.googleRefreshToken(encryptedToken);
        } else if (StringUtils.isBlank(existing.getGoogleRefreshToken())) {
            throw new IllegalArgumentException("Google refresh token is missing.");
        }

        settingRepository.save(builder.build());
    }

    /**
     * 新規に利用者を登録します。 メールアドレスまたは Slack ユーザー ID が既に存在する場合は例外をスローします。
     *
     * @param setting 登録するユーザー設定情報ドメインモデル
     * @return 保存後のユーザー設定情報ドメインモデル
     * @throws AlreadyExistsException 登録情報が既に存在する場合
     */
    public UserSetting registerUser(UserSetting setting) {
        if (settingRepository.findByEmail(setting.getEmail()).isPresent()) {
            throw new AlreadyExistsException("このメールアドレスは既に登録されています。");
        }
        if (settingRepository.findById(setting.getSlackUserId()).isPresent()) {
            throw new AlreadyExistsException("この Slack ユーザー ID は既に登録されています。");
        }
        settingRepository.save(setting);
        return setting;
    }
}
