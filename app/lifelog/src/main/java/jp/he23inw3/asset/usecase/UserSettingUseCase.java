package jp.he23inw3.asset.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import jp.he23inw3.asset.domain.service.UserSettingService;
import lombok.RequiredArgsConstructor;

/**
 * ユーザー個別設定の取得および保存フローを制御するユースケースクラス。
 * <p>
 * アダプター層からの要求に基づき、リポジトリを介して設定情報の検索および永続化を行います。
 */
@ApplicationScoped
@RequiredArgsConstructor
public class UserSettingUseCase {

    private final UserSettingRepository settingRepository;
    private final UserSettingService settingService;

    /**
     * 指定された Slack ユーザーIDの設定情報を取得します。
     *
     * @param slackUserId 取得対象 of Slack ユーザーID
     * @return ユーザー設定のドメインモデル
     * @throws ResourceNotFoundException 設定が存在しない場合
     */
    public UserSetting getSetting(String slackUserId) {
        return settingRepository.findById(slackUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User settings not found for " + slackUserId));
    }

    /**
     * ユーザー設定情報を保存または更新します。
     *
     * @param setting 保存する設定情報ドメインモデル
     * @return 保存後の設定情報ドメインモデル
     */
    public UserSetting saveSetting(UserSetting setting) {
        settingRepository.save(setting);
        return setting;
    }

    /**
     * 新規に利用者を登録します。
     * メールアドレスまたは Slack ユーザー ID が既に存在する場合は例外をスローします。
     *
     * @param setting 登録するユーザー設定情報ドメインモデル
     * @return 保存後の設定情報ドメインモデル
     * @throws AlreadyExistsException 登録情報が既に存在する場合
     */
    public UserSetting registerUser(UserSetting setting) {
        return settingService.registerUser(setting);
    }

    /**
     * すべてのユーザー設定一覧を取得します。
     *
     * @return すべてのユーザー設定のリスト
     */
    public List<UserSetting> getAllSettings() {
        return settingRepository.findAll();
    }

    /**
     * OIDC メールアドレスでユーザー設定を取得します。
     *
     * @param email OIDC メールアドレス
     * @return ユーザー設定のドメインモデル
     * @throws ResourceNotFoundException 設定が存在しない場合
     */
    public UserSetting getSettingByEmail(String email) {
        return settingRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User settings not found for email: " + email));
    }
}
