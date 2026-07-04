package jp.he23inw3.asset.infrastructure.firestore;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import jp.he23inw3.asset.domain.constant.FirestoreCollectionNames;
import jp.he23inw3.asset.domain.exception.ExternalServiceException;
import jp.he23inw3.asset.domain.model.AdminUser;
import jp.he23inw3.asset.domain.repository.AdminUserRepository;
import jp.he23inw3.asset.domain.util.InstantUtil;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Firestore {@code admin_users} コレクションの CRUD 処理を提供するリポジトリ実装クラス。
 * <p>
 * 管理者情報の検索、永続化、および初期ブートストラップ用の存在チェックを行います。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class FirestoreAdminUserRepositoryImpl implements AdminUserRepository {

    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_USER_NAME = "userName";
    private static final String FIELD_IS_ACTIVE = "isActive";
    private static final String FIELD_CREATED_BY = "createdBy";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_UPDATED_BY = "updatedBy";
    private static final String FIELD_UPDATED_AT = "updatedAt";

    private final Firestore firestore;

    /**
     * 指定されたメールアドレスに対応する管理者ユーザー情報を取得します。
     *
     * @param email 管理者のメールアドレス
     * @return 管理者情報を格納した {@link Optional}。存在しない場合は {@link Optional#empty()}
     * @throws ExternalServiceException
     *             Firestoreへのアクセスでエラーが発生した場合
     */
    @Override
    public Optional<AdminUser> findByEmail(String email) {
        try {
            DocumentSnapshot doc = firestore.collection(FirestoreCollectionNames.ADMIN_USERS)
                    .document(email)
                    .get()
                    .get();

            if (!doc.exists()) {
                return Optional.empty();
            }

            return Optional.of(mapDocumentToAdmin(doc));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("Firestoreからの管理者情報の取得に失敗しました。", e);
        }
    }

    /**
     * 管理者ユーザー情報を保存または更新します。
     *
     * @param adminUser 保存対象の管理者ユーザー設定ドメインモデル
     * @throws ExternalServiceException Firestoreへの保存処理でエラーが発生した場合
     */
    @Override
    public void save(AdminUser adminUser) {
        try {
            Map<String, Object> data = buildAdminDataMap(adminUser);

            firestore.collection(FirestoreCollectionNames.ADMIN_USERS)
                    .document(adminUser.getEmail())
                    .set(data)
                    .get();
            log.info(MessageHelper.getMessage("infra.firestore.admin.save"));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("Firestoreへの管理者情報の保存に失敗しました。", e);
        }
    }

    /**
     * 管理者ユーザーが1件も登録されていない（コレクションが空である）かを判定します。
     *
     * @return 登録済みの管理者が存在しない場合は {@code true}、1件以上存在する場合は {@code false}
     * @throws ExternalServiceException Firestoreへの件数クエリ処理でエラーが発生した場合
     */
    @Override
    public boolean isEmpty() {
        try {
            QuerySnapshot snapshot = firestore.collection(FirestoreCollectionNames.ADMIN_USERS)
                    .limit(1)
                    .get()
                    .get();
            return snapshot.isEmpty();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("Firestoreの管理者情報存在チェックに失敗しました。", e);
        }
    }

    /**
     * Firestore のドキュメントスナップショットから AdminUser ドメインモデルにマッピングします。
     *
     * @param doc Firestoreのドキュメントスナップショット
     * @return マッピングされた AdminUser オブジェクト
     */
    private AdminUser mapDocumentToAdmin(DocumentSnapshot doc) {
        Boolean activeVal = doc.getBoolean(FIELD_IS_ACTIVE);
        boolean isActive = activeVal != null && activeVal;

        Long createdAtLong = doc.getLong(FIELD_CREATED_AT);
        Instant createdAt = InstantUtil.toInstantOrNow(createdAtLong);

        Long updatedAtLong = doc.getLong(FIELD_UPDATED_AT);
        Instant updatedAt = InstantUtil.toInstantOrNow(updatedAtLong);

        return AdminUser.builder()
                .email(doc.getId())
                .userName(doc.getString(FIELD_USER_NAME))
                .active(isActive)
                .createdBy(doc.getString(FIELD_CREATED_BY))
                .createdAt(createdAt)
                .updatedBy(doc.getString(FIELD_UPDATED_BY))
                .updatedAt(updatedAt)
                .build();
    }

    /**
     * AdminUser ドメインモデルから Firestore 保存用のデータマップを構築します。
     *
     * @param adminUser 管理者ドメインモデル
     * @return Firestore保存用のマップ
     */
    private Map<String, Object> buildAdminDataMap(AdminUser adminUser) {
        Map<String, Object> data = new HashMap<>();
        data.put(FIELD_EMAIL, adminUser.getEmail());
        data.put(FIELD_USER_NAME, adminUser.getUserName());
        data.put(FIELD_IS_ACTIVE, adminUser.isActive());
        data.put(FIELD_CREATED_BY, adminUser.getCreatedBy());
        data.put(FIELD_CREATED_AT, InstantUtil.toEpochSecondOrNow(adminUser.getCreatedAt()));
        data.put(FIELD_UPDATED_BY, adminUser.getUpdatedBy());
        data.put(FIELD_UPDATED_AT, InstantUtil.toEpochSecondOrNow(adminUser.getUpdatedAt()));
        return data;
    }
}
