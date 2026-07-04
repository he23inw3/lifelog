package jp.he23inw3.asset.infrastructure.google;

import com.google.api.services.calendar.Calendar;
import jakarta.enterprise.context.ApplicationScoped;
import jp.he23inw3.asset.domain.model.HealthStatus;
import jp.he23inw3.asset.domain.repository.ExternalHealthRepository;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Google Calendar API の接続状態（ヘルスチェック）を検査する専用の実装クラス。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class GoogleCalendarHealthRepositoryImpl implements ExternalHealthRepository {

    private final Calendar calendarService;

    /**
     * サービス名として "Google Calendar" を返します。
     *
     * @return サービス名
     */
    @Override
    public String getServiceName() {
        return "Google Calendar";
    }

    /**
     * Google Calendar API の疎通テストを実行し、結果を返します。
     *
     * @return 接続状態
     */
    @Override
    public HealthStatus checkHealth() {
        try {
            calendarService.calendarList().list().setMaxResults(1).execute();
            return HealthStatus.UP;
        } catch (Exception e) {
            log.error(MessageHelper.getMessage("infra.health.calendar.error"), e);
            return HealthStatus.DOWN;
        }
    }
}
