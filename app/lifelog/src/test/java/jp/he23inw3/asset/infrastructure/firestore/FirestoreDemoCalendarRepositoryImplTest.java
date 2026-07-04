package jp.he23inw3.asset.infrastructure.firestore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.time.LocalDate;
import java.util.List;
import jp.he23inw3.asset.domain.constant.FirestoreCollectionNames;
import jp.he23inw3.asset.domain.model.DemoCalendarEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FirestoreDemoCalendarRepositoryImplTest {

    @Mock
    Firestore firestore;

    @Mock
    CollectionReference collectionReference;

    @Mock
    Query query;

    @Mock
    QuerySnapshot querySnapshot;

    @Mock
    QueryDocumentSnapshot queryDocumentSnapshot;

    FirestoreDemoCalendarRepositoryImpl target;

    @BeforeEach
    void setUp() {
        target = new FirestoreDemoCalendarRepositoryImpl(firestore);
    }

    @Nested
    @DisplayName("findByCalendarIdAndPeriodメソッドのテスト")
    class FindByCalendarIdAndPeriod {

        @Test
        @DisplayName("カレンダーIDと期間にマッチするデモ予定が正常に返ること")
        void testFindByCalendarIdAndPeriod() throws Exception {
            String calendarId = "calendar-123";
            LocalDate start = LocalDate.of(2026, 6, 1);
            LocalDate end = LocalDate.of(2026, 6, 30);

            when(firestore.collection(FirestoreCollectionNames.DEMO_CALENDAR_EVENTS)).thenReturn(collectionReference);
            when(collectionReference.whereEqualTo("calendarId", calendarId)).thenReturn(query);
            when(query.whereGreaterThanOrEqualTo("date", "2026-06-01")).thenReturn(query);
            when(query.whereLessThanOrEqualTo("date", "2026-06-30")).thenReturn(query);
            when(query.get()).thenReturn(ApiFutures.immediateFuture(querySnapshot));

            when(querySnapshot.getDocuments()).thenReturn(List.of(queryDocumentSnapshot));
            when(queryDocumentSnapshot.getString("calendarId")).thenReturn(calendarId);
            when(queryDocumentSnapshot.getString("date")).thenReturn("2026-06-15");
            when(queryDocumentSnapshot.getString("title")).thenReturn("会議");
            when(queryDocumentSnapshot.getString("description")).thenReturn("定例");
            when(queryDocumentSnapshot.getBoolean("holiday")).thenReturn(false);
            when(queryDocumentSnapshot.getLong("syncedAt")).thenReturn(1719734400L);

            List<DemoCalendarEvent> result = target.findByCalendarIdAndPeriod(calendarId, start, end);

            assertThat(result).hasSize(1);
            DemoCalendarEvent event = result.get(0);
            assertThat(event.getCalendarId()).isEqualTo(calendarId);
            assertThat(event.getDate()).isEqualTo("2026-06-15");
            assertThat(event.getTitle()).isEqualTo("会議");
            assertThat(event.isHoliday()).isFalse();
            assertThat(event.getSyncedAt()).isNotNull();
        }
    }
}
