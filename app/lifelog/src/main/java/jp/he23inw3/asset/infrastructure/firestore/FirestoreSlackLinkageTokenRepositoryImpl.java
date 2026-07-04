package jp.he23inw3.asset.infrastructure.firestore;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import jp.he23inw3.asset.domain.constant.FirestoreCollectionNames;
import jp.he23inw3.asset.domain.exception.ExternalServiceException;
import jp.he23inw3.asset.domain.model.SlackLinkageToken;
import jp.he23inw3.asset.domain.repository.SlackLinkageTokenRepository;
import jp.he23inw3.asset.domain.util.InstantUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Firestore {@code slack_linkage_tokens} コレクションの CRUD 実装。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class FirestoreSlackLinkageTokenRepositoryImpl implements SlackLinkageTokenRepository {

    private static final String FIELD_SLACK_USER_ID = "slackUserId";
    private static final String FIELD_EXPIRES_AT = "expiresAt";

    private final Firestore firestore;

    /**
     * 一時トークン情報を保存します。
     *
     * @param linkageToken 保存対象のトークン情報
     */
    @Override
    public void save(SlackLinkageToken linkageToken) {
        try {
            DocumentReference docRef = firestore.collection(FirestoreCollectionNames.SLACK_LINKAGE_TOKENS)
                    .document(linkageToken.getToken());

            Map<String, Object> data = new HashMap<>();
            data.put(FIELD_SLACK_USER_ID, linkageToken.getSlackUserId());
            data.put(FIELD_EXPIRES_AT, InstantUtil.toEpochSecond(linkageToken.getExpiresAt()));

            docRef.set(data).get();
        } catch (InterruptedException | ExecutionException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ExternalServiceException("Firestoreへの連携トークンの保存に失敗しました。", e);
        }
    }

    /**
     * トークンを条件に、一時トークン情報を取得します。
     * @param token トークン
     * @return 一時トークン情報が格納された Optional。存在しない場合は empty
     */
    @Override
    public Optional<SlackLinkageToken> findByToken(String token) {
        try {
            DocumentSnapshot doc = firestore.collection(FirestoreCollectionNames.SLACK_LINKAGE_TOKENS)
                    .document(token)
                    .get()
                    .get();

            if (!doc.exists()) {
                return Optional.empty();
            }

            Long expiresAtLong = doc.getLong(FIELD_EXPIRES_AT);
            Instant expiresAt = InstantUtil.toInstant(expiresAtLong);

            return Optional.of(SlackLinkageToken.builder()
                    .token(token)
                    .slackUserId(doc.getString(FIELD_SLACK_USER_ID))
                    .expiresAt(expiresAt)
                    .build());
        } catch (InterruptedException | ExecutionException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ExternalServiceException("Firestoreからの連携トークンの取得に失敗しました。", e);
        }
    }

    /**
     * 一時トークン情報を削除します。
     * @param token 削除対象のトークン
     */
    @Override
    public void delete(String token) {
        try {
            firestore.collection(FirestoreCollectionNames.SLACK_LINKAGE_TOKENS)
                    .document(token)
                    .delete()
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ExternalServiceException("Firestoreからの連携トークンの削除に失敗しました。", e);
        }
    }
}
