package jp.he23inw3.asset.infrastructure.firestore;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import jp.he23inw3.asset.domain.constant.FirestoreCollectionNames;
import jp.he23inw3.asset.domain.exception.ExternalServiceException;
import jp.he23inw3.asset.domain.model.Session;
import jp.he23inw3.asset.domain.model.SessionStatus;
import jp.he23inw3.asset.domain.repository.UserSessionRepository;
import jp.he23inw3.asset.domain.util.InstantUtil;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Firestore {@code user_sessions} コレクション of CRUD 実装。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class FirestoreSessionRepositoryImpl implements UserSessionRepository {

    private static final String FIELD_SLACK_USER_ID = "slackUserId";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_UPDATED_AT = "updatedAt";
    private static final String FIELD_TEMP_DATA = "tempData";
    private static final String FIELD_EXPIRES_AT = "expiresAt";

    private final Firestore firestore;

    /**
     * 対話セッション情報を保存または更新します。
     *
     * @param session
     *            保存対象の対話セッションドメインモデル
     */
    @Override
    public void save(Session session) {
        try {
            DocumentReference docRef = firestore.collection(FirestoreCollectionNames.USER_SESSIONS)
                    .document(session.getSlackUserId());

            Map<String, Object> data = new HashMap<>();
            data.put(FIELD_SLACK_USER_ID, session.getSlackUserId());
            data.put(FIELD_STATUS, session.getStatus() != null ? session.getStatus().name() : null);
            data.put(FIELD_UPDATED_AT, InstantUtil.toEpochSecondOrNow(session.getUpdatedAt()));

            if (session.getTempData() != null) {
                data.put(FIELD_TEMP_DATA, session.getTempData());
            } else {
                data.put(FIELD_TEMP_DATA, new HashMap<String, String>());
            }

            data.put(FIELD_EXPIRES_AT, InstantUtil.toEpochSecond(session.getExpiresAt()));

            docRef.set(data).get();
            log.info(MessageHelper.getMessage("infra.firestore.session.save", session.getSlackUserId()));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("Firestoreへのセッションの保存に失敗しました。", e);
        }
    }

    /**
     * 指定された Slack ユーザー ID を持つ対話セッションを取得します。
     *
     * @param slackUserId
     *            ユーザーID
     * @return セッションが存在する場合はそのセッション、存在しない場合は空の Optional
     */
    @Override
    public Optional<Session> findById(String slackUserId) {
        try {
            DocumentSnapshot doc = firestore.collection(FirestoreCollectionNames.USER_SESSIONS).document(slackUserId)
                    .get().get();

            if (!doc.exists()) {
                return Optional.empty();
            }

            return Optional.of(mapDocumentToSession(doc));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("Firestoreからのセッションの取得に失敗しました。", e);
        }
    }

    /**
     * 指定された Slack ユーザー ID に紐づく対話セッションを削除します。
     *
     * @param slackUserId
     *            削除対象のユーザーID
     */
    @Override
    public void delete(String slackUserId) {
        try {
            firestore.collection(FirestoreCollectionNames.USER_SESSIONS).document(slackUserId).delete().get();
            log.info(MessageHelper.getMessage("infra.firestore.session.delete", slackUserId));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("Firestoreからのセッションの削除に失敗しました。", e);
        }
    }

    /**
     * すべての対話セッション一覧を取得します。
     *
     * @return 対話セッションのリスト
     */
    @Override
    public List<Session> findAll() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(FirestoreCollectionNames.USER_SESSIONS).get().get();
            List<Session> sessions = new ArrayList<>();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                sessions.add(mapDocumentToSession(doc));
            }
            return sessions;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("Firestoreからのセッション一覧の取得に失敗しました。", e);
        }
    }

    private Session mapDocumentToSession(DocumentSnapshot doc) {
        Long updatedAtLong = doc.getLong(FIELD_UPDATED_AT);
        Instant updatedAt = InstantUtil.toInstantOrNow(updatedAtLong);

        Map<String, String> tempData = new HashMap<>();
        Object rawTempData = doc.get(FIELD_TEMP_DATA);
        if (rawTempData instanceof Map<?, ?>) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) rawTempData).entrySet()) {
                if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                    tempData.put((String) entry.getKey(), (String) entry.getValue());
                }
            }
        }

        return Session.builder()
                .slackUserId(doc.getString(FIELD_SLACK_USER_ID))
                .status(SessionStatus.fromValue(doc.getString(FIELD_STATUS)))
                .updatedAt(updatedAt)
                .tempData(tempData)
                .expiresAt(InstantUtil.toInstant(doc.getLong(FIELD_EXPIRES_AT)))
                .build();
    }
}
