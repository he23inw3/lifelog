package jp.he23inw3.asset.infrastructure.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.exception.GatewayException;
import jp.he23inw3.asset.domain.gateway.CryptoGateway;
import jp.he23inw3.asset.domain.gateway.GoogleCalendarGateway;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Google Calendar API v3 を使用した GoogleCalendarGateway 実装。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class GoogleCalendarGatewayImpl implements GoogleCalendarGateway {

    private static final String APPLICATION_NAME = "LifeLog";
    private static final ZoneId JAPAN_ZONE = DateTimeUtil.TOKYO_ZONE;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final LifeLogConfig config;
    private final UserSettingRepository userSettingRepository;
    private final CryptoGateway cryptoGateway;

    private HttpTransport httpTransport;
    private Calendar defaultCalendarService;

    private synchronized HttpTransport getHttpTransport() {
        if (httpTransport == null) {
            try {
                httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            } catch (GeneralSecurityException | IOException e) {
                log.error(MessageHelper.getMessage("infra.calendar.transport.init.error"), e);
                throw new GatewayException("Google NetHttpTransportの初期化に失敗しました。", e);
            }
        }
        return httpTransport;
    }

    private synchronized Calendar getDefaultCalendarService() {
        if (defaultCalendarService == null) {
            try {
                GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                        .createScoped(Collections.singletonList(CalendarScopes.CALENDAR));
                defaultCalendarService = new Calendar.Builder(getHttpTransport(), JSON_FACTORY,
                        new HttpCredentialsAdapter(credentials)).setApplicationName(APPLICATION_NAME).build();
                log.info(MessageHelper.getMessage("infra.calendar.init"));
            } catch (IOException e) {
                throw new GatewayException("Google Calendar API サービスの初期化に失敗しました。", e);
            }
        }
        return defaultCalendarService;
    }

    @Override
    public boolean isHolidayOrPaidLeave(String calendarId, LocalDate date) {
        // ユーザー固有の OAuth クライアントを取得（未連携の場合は ADC にフォールバック）
        Calendar service = getCalendarService(calendarId);

        // 日本の祝日カレンダーのチェック
        // 祝日カレンダーは公開カレンダーのため、calendar スコープを持つ
        // ユーザー OAuth トークンでもアクセス可能
        String holidayCalendarId = config.google().japaneseHolidayCalendarId();
        if (hasEventOnDate(service, holidayCalendarId, date, null)) {
            log.info(MessageHelper.getMessage("infra.calendar.holiday", date));
            return true;
        }

        // ユーザーカレンダーの「有給休暇・休暇」チェック
        if (hasEventOnDate(service, calendarId, date, List.of("有休", "有給", "休暇", "休み"))) {
            log.info(MessageHelper.getMessage("infra.calendar.leave", date, calendarId));
            return true;
        }

        return false;
    }

    @Override
    public void insertOrUpdateEvent(String calendarId, LocalDate date, String title, String description) {
        Calendar service = getCalendarService(calendarId);
        try {
            // 対象日の既存の日報イベントを検索
            Event existingEvent = findDailyLogEvent(service, calendarId, date);

            EventDateTime start = new EventDateTime().setDate(new DateTime(date.toString())).setTimeZone("Asia/Tokyo");
            EventDateTime end = new EventDateTime().setDate(new DateTime(date.plusDays(1).toString()))
                    .setTimeZone("Asia/Tokyo");

            if (existingEvent != null) {
                // 更新
                existingEvent.setSummary(title);
                existingEvent.setDescription(description);
                existingEvent.setStart(start);
                existingEvent.setEnd(end);
                service.events().update(calendarId, existingEvent.getId(), existingEvent).execute();
                log.info(MessageHelper.getMessage("infra.calendar.update", calendarId, date));
            } else {
                // 新規挿入
                Event newEvent = new Event().setSummary(title).setDescription(description).setStart(start).setEnd(end);
                service.events().insert(calendarId, newEvent).execute();
                log.info(MessageHelper.getMessage("infra.calendar.insert", calendarId, date));
            }
        } catch (IOException e) {
            throw new GatewayException("Google Calendar の予定の登録または更新に失敗しました。", e);
        }
    }

    /**
     * ユーザー固有の Google Calendar 連携クライアントを動的に生成・取得します。
     * <p>
     * 指定された {@code calendarId} に対応するユーザー設定（{@link UserSetting}）を検索し、
     * 登録されている暗号化されたリフレッシュトークンを {@link CryptoGateway} で復号した上で、 ユーザー個別の
     * {@link UserCredentials} を構築します。
     * </p>
     * <p>
     * 構築された {@link UserCredentials} を用いることで、アクセストークンの有効期限が切れた場合でも、 Google
     * の認証ライブラリが自動的かつネイティブにリフレッシュトークンを使用してトークンを更新します。
     * したがって、アプリケーション層で手動のトークン更新処理をトリガーする必要はありません。
     * </p>
     * <p>
     * 該当する連携設定が見つからない場合、リフレッシュトークンが未登録の場合、あるいはエラーが発生した場合は、
     * アプリケーションのデフォルトの認証情報（Application Default Credentials）を持つ共有クライアントを返します。
     * </p>
     *
     * @param calendarId
     *            Google カレンダーID（通常は利用者のメールアドレス）
     * @return {@link Calendar} サービスインスタンス
     */
    private Calendar getCalendarService(String calendarId) {
        if (config.demo().enabled()) {
            return getDefaultCalendarService();
        }

        try {
            Optional<UserSetting> settingOpt = userSettingRepository.findByEmail(calendarId);
            if (settingOpt.isEmpty()) {
                settingOpt = userSettingRepository.findAll().stream()
                        .filter(s -> calendarId.equalsIgnoreCase(s.getGoogleCalendarId())).findFirst();
            }

            if (settingOpt.isPresent()) {
                UserSetting setting = settingOpt.get();
                String encryptedToken = setting.getGoogleRefreshToken();

                if (StringUtils.isNotBlank(encryptedToken)) {
                    String refreshToken = cryptoGateway.decrypt(encryptedToken);
                    UserCredentials credentials = UserCredentials.newBuilder()
                            .setClientId(config.google().oauthClientId())
                            .setClientSecret(config.google().oauthClientSecret())
                            .setRefreshToken(refreshToken)
                            .build();

                    return new Calendar.Builder(getHttpTransport(), JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                            .setApplicationName(APPLICATION_NAME)
                            .build();
                }
            }
        } catch (Exception e) {
            log.warn(MessageHelper.getMessage("infra.calendar.user.client.error", calendarId), e);
        }

        return getDefaultCalendarService();
    }

    private boolean hasEventOnDate(Calendar service, String calendarId, LocalDate date, List<String> keywords) {
        try {
            ZonedDateTime startOfDay = date.atStartOfDay(JAPAN_ZONE);
            ZonedDateTime endOfDay = date.plusDays(1).atStartOfDay(JAPAN_ZONE);

            DateTime timeMin = new DateTime(startOfDay.toInstant().toEpochMilli());
            DateTime timeMax = new DateTime(endOfDay.toInstant().toEpochMilli());

            String pageToken = null;
            do {
                Events events = service.events().list(calendarId)
                        .setTimeMin(timeMin)
                        .setTimeMax(timeMax)
                        .setSingleEvents(true)
                        .setOrderBy("startTime")
                        .setPageToken(pageToken)
                        .execute();

                List<Event> items = events.getItems();
                if (!CollectionUtils.isEmpty(items)) {
                    if (CollectionUtils.isEmpty(keywords)) {
                        return true; // イベントが存在すれば何でも一致とする（祝日カレンダー用）
                    }
                    for (Event event : items) {
                        String summary = event.getSummary();
                        if (StringUtils.isNotBlank(summary)) {
                            for (String keyword : keywords) {
                                if (summary.contains(keyword)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
                pageToken = events.getNextPageToken();
            } while (pageToken != null);

            return false;
        } catch (IOException e) {
            log.warn(MessageHelper.getMessage("infra.calendar.check.error", calendarId), e);
            return false;
        }
    }

    private Event findDailyLogEvent(Calendar service, String calendarId, LocalDate date) throws IOException {
        ZonedDateTime startOfDay = date.atStartOfDay(JAPAN_ZONE);
        ZonedDateTime endOfDay = date.plusDays(1).atStartOfDay(JAPAN_ZONE);

        DateTime timeMin = new DateTime(startOfDay.toInstant().toEpochMilli());
        DateTime timeMax = new DateTime(endOfDay.toInstant().toEpochMilli());

        Events events = service.events()
                .list(calendarId)
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .setSingleEvents(true)
                .execute();

        List<Event> items = events.getItems();
        for (Event event : items) {
            String summary = event.getSummary();
            if (StringUtils.isNotBlank(summary) && summary.startsWith("[日報]")) {
                return event;
            }
        }
        return null;
    }
}
