package jp.he23inw3.asset.infrastructure.demo;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.time.Instant;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import jp.he23inw3.asset.domain.util.InstantUtil;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * デモモードが有効な場合に、起動時に自動的にデモユーザー設定を登録するイニシャライザ。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DemoUserInitializer {

    private final LifeLogConfig config;

    private final UserSettingRepository userSettingRepository;

    /**
     * アプリケーション起動時に実行される初期化メソッド。
     *
     * @param ev 起動イベント
     */
    void onStart(@Observes StartupEvent ev) {
        if (config.demo().enabled()) {
            String email = config.demo().userEmail();
            String slackUserId = config.demo().slackUserId();
            log.info(MessageHelper.getMessage("infra.demo.user.init.check", email, slackUserId));

            if (userSettingRepository.findByEmail(email).isEmpty()) {
                Instant now = InstantUtil.now();
                UserSetting demoUser = UserSetting.builder()
                        .slackUserId(slackUserId)
                        .email(email)
                        .userName("デモユーザー")
                        .remindTime("09:00")
                        .googleCalendarId(email)
                        .active(true)
                        .googleLinked(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                userSettingRepository.save(demoUser);
                log.info(MessageHelper.getMessage("infra.demo.user.init.register", demoUser));
            } else {
                log.info(MessageHelper.getMessage("infra.demo.user.init.exists"));
            }
        }
    }
}
