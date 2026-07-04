package jp.he23inw3.asset.infrastructure.demo;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import jp.he23inw3.asset.domain.constant.FirestoreCollectionNames;
import jp.he23inw3.asset.domain.exception.ExternalServiceException;
import jp.he23inw3.asset.domain.gateway.GoogleCalendarGateway;
import jp.he23inw3.asset.domain.util.InstantUtil;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;

/**
 * デモモード用の擬似カレンダーゲートウェイ実装。
 * <p>
 * Google Calendar API の代わりに Firestore コレクション {@code demo_calendar_events} に
 * カレンダーイベントを永続化します。 ドキュメントキーは {@code {calendarId}_{date}} の形式です。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DemoCalendarGatewayImpl implements GoogleCalendarGateway {

    private final Firestore firestore;

    /**
     * 指定された日付が休暇イベントとして登録されているかを Firestore で確認します。
     *
     * @param calendarId 対象のカレンダーID
     * @param date 判定対象の日付
     * @return 休暇イベントが存在する場合は true
     */
    @Override
    public boolean isHolidayOrPaidLeave(String calendarId, LocalDate date) {
        try {
            String docId = buildDocId(calendarId, date);
            DocumentSnapshot doc = firestore.collection(FirestoreCollectionNames.DEMO_CALENDAR_EVENTS).document(docId)
                    .get().get();
            if (!doc.exists()) {
                return false;
            }
            Boolean holiday = doc.getBoolean("holiday");
            return BooleanUtils.isTrue(holiday);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.warn(MessageHelper.getMessage("infra.demo.calendar.check.error", e.getMessage()));
            return false;
        }
    }

    /**
     * Firestore にカレンダーイベントを登録または更新します。
     *
     * @param calendarId 対象のカレンダーID
     * @param date 登録・更新対象の日付
     * @param title イベントのタイトル
     * @param description イベントの説明
     */
    @Override
    public void insertOrUpdateEvent(String calendarId, LocalDate date, String title, String description) {
        try {
            String collectionName = FirestoreCollectionNames.DEMO_CALENDAR_EVENTS;
            String docId = buildDocId(calendarId, date);
            DocumentReference docRef = firestore.collection(collectionName).document(docId);

            boolean isHoliday = title != null && title.contains("休暇");
            Map<String, Object> data = new HashMap<>();
            data.put("calendarId", calendarId);
            data.put("date", date.toString());
            data.put("title", title);
            data.put("description", description);
            data.put("holiday", isHoliday);
            data.put("syncedAt", InstantUtil.nowEpochSecond());

            docRef.set(data).get();
            log.info(MessageHelper.getMessage("infra.demo.calendar.save", date, title));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("デモカレンダーへのイベント登録に失敗しました。", e);
        }
    }

    /**
     * Firestore のドキュメントIDに使用できない文字（/ 等）を置換してキーを生成します。
     *
     * @param calendarId
     *            カレンダーID
     * @param date
     *            日付
     * @return Firestore のドキュメントID形式の文字列
     */
    private String buildDocId(String calendarId, LocalDate date) {
        String safeCalendarId = calendarId.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        return safeCalendarId + "_" + date;
    }
}
