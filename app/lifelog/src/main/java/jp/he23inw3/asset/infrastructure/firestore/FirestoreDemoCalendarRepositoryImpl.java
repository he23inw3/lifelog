package jp.he23inw3.asset.infrastructure.firestore;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import jp.he23inw3.asset.domain.constant.FirestoreCollectionNames;
import jp.he23inw3.asset.domain.exception.ExternalServiceException;
import jp.he23inw3.asset.domain.model.DemoCalendarEvent;
import jp.he23inw3.asset.domain.repository.DemoCalendarRepository;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import jp.he23inw3.asset.domain.util.InstantUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Firestore をデータストアとした DemoCalendarRepository の実装クラス。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class FirestoreDemoCalendarRepositoryImpl implements DemoCalendarRepository {

    private final Firestore firestore;

    /**
     * 指定されたカレンダーIDと期間に該当するカレンダーイベント一覧を取得します。
     *
     * @param calendarId カレンダーID
     * @param start 期間開始日
     * @param end 期間終了日
     * @return カレンダーイベントのリスト
     */
    @Override
    public List<DemoCalendarEvent> findByCalendarIdAndPeriod(String calendarId, LocalDate start, LocalDate end) {
        try {
            String collectionName = FirestoreCollectionNames.DEMO_CALENDAR_EVENTS;
            QuerySnapshot querySnapshot = firestore.collection(collectionName)
                    .whereEqualTo("calendarId", calendarId)
                    .whereGreaterThanOrEqualTo("date", start.toString())
                    .whereLessThanOrEqualTo("date", end.toString())
                    .get()
                    .get();

            return querySnapshot.getDocuments().stream().map(doc -> {
                Long syncedAtLong = doc.getLong("syncedAt");
                LocalDateTime syncedAt = DateTimeUtil.toLocalDateTime(InstantUtil.toInstant(syncedAtLong));
                return DemoCalendarEvent.builder()
                        .calendarId(doc.getString("calendarId"))
                        .date(doc.getString("date"))
                        .title(doc.getString("title"))
                        .description(doc.getString("description"))
                        .holiday(doc.getBoolean("holiday") != null && doc.getBoolean("holiday"))
                        .syncedAt(syncedAt)
                        .build();
            }).collect(Collectors.toList());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("デモカレンダーイベントの取得トランザクションが中断されました。", e);
        } catch (ExecutionException e) {
            throw new ExternalServiceException("デモカレンダーイベントの取得クエリ実行に失敗しました。", e);
        }
    }
}
