package jp.he23inw3.asset.infrastructure.firestore;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import jp.he23inw3.asset.domain.constant.FirestoreCollectionNames;
import jp.he23inw3.asset.domain.exception.ExternalServiceException;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import jp.he23inw3.asset.domain.util.InstantUtil;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Firestore {@code user_settings} コレクションの CRUD 実装。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class FirestoreSettingRepositoryImpl implements UserSettingRepository {

    private static final String FIELD_SLACK_USER_ID = "slackUserId";
    private static final String FIELD_USER_NAME = "userName";
    private static final String FIELD_REMIND_TIME = "remindTime";
    private static final String FIELD_IS_ACTIVE = "isActive";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_UPDATED_AT = "updatedAt";

    private static final String FIELD_GOOGLE_EMAIL = "googleEmail";
    private static final String FIELD_GOOGLE_CALENDAR_ID = "googleCalendarId";
    private static final String FIELD_ENCRYPTED_REFRESH_TOKEN = "encryptedRefreshToken";

    private final Firestore firestore;

    /**
     * ユーザー設定情報を保存または更新します。
     *
     * @param setting
     *            保存対象のユーザー設定ドメインモデル
     */
    @Override
    public void save(UserSetting setting) {
        try {
            // 1. user_settings コレクションの保存
            DocumentReference settingsDocRef = firestore.collection(FirestoreCollectionNames.USER_SETTINGS)
                    .document(setting.getSlackUserId());

            Map<String, Object> settingsData = new HashMap<>();
            settingsData.put(FIELD_SLACK_USER_ID, setting.getSlackUserId());
            settingsData.put(FIELD_USER_NAME, setting.getUserName());
            settingsData.put(FIELD_REMIND_TIME, setting.getRemindTime());
            settingsData.put(FIELD_IS_ACTIVE, setting.isActive());
            settingsData.put(FIELD_CREATED_AT, InstantUtil.toEpochSecondOrNow(setting.getCreatedAt()));
            settingsData.put(FIELD_UPDATED_AT, InstantUtil.toEpochSecondOrNow(setting.getUpdatedAt()));

            settingsDocRef.set(settingsData).get();

            // 2. user_credentials コレクションの保存
            if (!StringUtils.isAllBlank(setting.getEmail(), setting.getGoogleCalendarId(), setting.getGoogleRefreshToken())) {
                String credentialsCollection = FirestoreCollectionNames.USER_CREDENTIALS;
                DocumentReference credentialsDocRef = firestore.collection(credentialsCollection)
                        .document(setting.getSlackUserId());

                Map<String, Object> credentialsData = new HashMap<>();
                credentialsData.put(FIELD_SLACK_USER_ID, setting.getSlackUserId());
                credentialsData.put(FIELD_GOOGLE_EMAIL, setting.getEmail());
                credentialsData.put(FIELD_GOOGLE_CALENDAR_ID, setting.getGoogleCalendarId());

                String encryptedToken = setting.getGoogleRefreshToken();
                credentialsData.put(FIELD_ENCRYPTED_REFRESH_TOKEN, encryptedToken);

                credentialsDocRef.set(credentialsData).get();
            }

            log.info(MessageHelper.getMessage("infra.firestore.setting.save", setting.getSlackUserId()));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("Firestoreへのユーザー設定の保存に失敗しました。", e);
        }
    }

    /**
     * 指定された Slack ユーザーIDに対応する設定情報を取得します。
     *
     * @param slackUserId
     *            Slack ユーザーID
     * @return ユーザー設定を格納した {@link Optional}。存在しない場合は {@link Optional#empty()}
     */
    @Override
    public Optional<UserSetting> findById(String slackUserId) {
        try {
            DocumentSnapshot settingsDoc = firestore.collection(FirestoreCollectionNames.USER_SETTINGS)
                    .document(slackUserId)
                    .get()
                    .get();

            if (!settingsDoc.exists()) {
                return Optional.empty();
            }

            DocumentSnapshot credentialsDoc = firestore.collection(FirestoreCollectionNames.USER_CREDENTIALS)
                    .document(slackUserId)
                    .get()
                    .get();

            return Optional.of(mapDocumentsToSetting(settingsDoc, credentialsDoc));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("Firestoreからのユーザー設定の取得に失敗しました。", e);
        }
    }

    /**
     * リマインド通知が有効（active = true）なすべてのユーザー設定一覧を取得します。
     *
     * @return 有効なユーザー設定のリスト
     */
    @Override
    public List<UserSetting> findAllActive() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(FirestoreCollectionNames.USER_SETTINGS)
                    .whereEqualTo(FIELD_IS_ACTIVE, true)
                    .get()
                    .get();

            return mergeSettingsWithCredentials(querySnapshot.getDocuments().stream().map(doc -> (DocumentSnapshot) doc)
                    .collect(Collectors.toList()));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("Firestoreからのアクティブなユーザー設定一覧の取得に失敗しました。", e);
        }
    }

    /**
     * すべてのユーザー設定一覧を取得します。
     *
     * @return すべて of ユーザー設定のリスト
     */
    @Override
    public List<UserSetting> findAll() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(FirestoreCollectionNames.USER_SETTINGS).get().get();

            return mergeSettingsWithCredentials(querySnapshot.getDocuments().stream()
                    .map(doc -> (DocumentSnapshot) doc)
                    .collect(Collectors.toList()));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("Firestoreからのユーザー設定一覧の取得に失敗しました。", e);
        }
    }

    /**
     * 指定されたメールアドレスに対応するユーザー設定を取得します。
     *
     * @param email OIDC メールアドレス
     * @return ユーザー設定を格納した {@link Optional}
     */
    @Override
    public Optional<UserSetting> findByEmail(String email) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(FirestoreCollectionNames.USER_CREDENTIALS)
                    .whereEqualTo(FIELD_GOOGLE_EMAIL, email)
                    .limit(1)
                    .get()
                    .get();

            if (querySnapshot.isEmpty()) {
                return Optional.empty();
            }

            DocumentSnapshot credentialsDoc = querySnapshot.getDocuments().get(0);
            String slackUserId = credentialsDoc.getId();

            DocumentSnapshot settingsDoc = firestore.collection(FirestoreCollectionNames.USER_SETTINGS)
                    .document(slackUserId)
                    .get()
                    .get();

            if (!settingsDoc.exists()) {
                return Optional.empty();
            }

            return Optional.of(mapDocumentsToSetting(settingsDoc, credentialsDoc));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("Firestore からのメールアドレス検索に失敗しました。", e);
        }
    }

    /**
     * 指定された Slack ユーザーIDに対応する設定情報を削除します。
     *
     * @param slackUserId Slack ユーザーID
     */
    @Override
    public void delete(String slackUserId) {
        try {
            firestore.collection(FirestoreCollectionNames.USER_SETTINGS)
                    .document(slackUserId)
                    .delete()
                    .get();
            firestore.collection(FirestoreCollectionNames.USER_CREDENTIALS)
                    .document(slackUserId)
                    .delete()
                    .get();
            log.info(MessageHelper.getMessage("infra.firestore.setting.delete", slackUserId));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("Firestoreからのユーザー設定の削除に失敗しました。", e);
        }
    }

    /**
     * 古い Slack ユーザーIDのデータを削除し、新しい設定データをアトミックに保存します。
     *
     * @param oldSlackUserId 削除対象の古い Slack ユーザーID
     * @param newSetting 保存対象の新しいユーザー設定ドメインモデル
     */
    @Override
    public void saveWithMigration(String oldSlackUserId, UserSetting newSetting) {
        try {
            firestore.runTransaction(transaction -> {
                // 1. 古い設定/認証情報を削除
                DocumentReference oldSettingsDoc = firestore.collection(FirestoreCollectionNames.USER_SETTINGS)
                        .document(oldSlackUserId);
                transaction.delete(oldSettingsDoc);

                DocumentReference oldCredentialsDoc = firestore.collection(FirestoreCollectionNames.USER_CREDENTIALS)
                        .document(oldSlackUserId);
                transaction.delete(oldCredentialsDoc);

                // 2. 新しい設定情報を保存
                DocumentReference newSettingsDoc = firestore.collection(FirestoreCollectionNames.USER_SETTINGS)
                        .document(newSetting.getSlackUserId());
                Map<String, Object> settingsData = new HashMap<>();
                settingsData.put(FIELD_SLACK_USER_ID, newSetting.getSlackUserId());
                settingsData.put(FIELD_USER_NAME, newSetting.getUserName());
                settingsData.put(FIELD_REMIND_TIME, newSetting.getRemindTime());
                settingsData.put(FIELD_IS_ACTIVE, newSetting.isActive());
                settingsData.put(FIELD_CREATED_AT, InstantUtil.toEpochSecondOrNow(newSetting.getCreatedAt()));
                settingsData.put(FIELD_UPDATED_AT, InstantUtil.toEpochSecondOrNow(newSetting.getUpdatedAt()));
                transaction.set(newSettingsDoc, settingsData);

                // 3. 新しい認証情報を保存
                if (!StringUtils.isAllBlank(newSetting.getEmail(), newSetting.getGoogleCalendarId(), newSetting.getGoogleRefreshToken())) {
                    DocumentReference newCredentialsDoc = firestore
                            .collection(FirestoreCollectionNames.USER_CREDENTIALS)
                            .document(newSetting.getSlackUserId());
                    Map<String, Object> credentialsData = new HashMap<>();
                    credentialsData.put(FIELD_SLACK_USER_ID, newSetting.getSlackUserId());
                    credentialsData.put(FIELD_GOOGLE_EMAIL, newSetting.getEmail());
                    credentialsData.put(FIELD_GOOGLE_CALENDAR_ID, newSetting.getGoogleCalendarId());
                    credentialsData.put(FIELD_ENCRYPTED_REFRESH_TOKEN, newSetting.getGoogleRefreshToken());
                    transaction.set(newCredentialsDoc, credentialsData);
                }

                return null;
            }).get();
            log.info(MessageHelper.getMessage("infra.firestore.setting.migrate", oldSlackUserId, newSetting.getSlackUserId()));
        } catch (InterruptedException | ExecutionException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ExternalServiceException("Firestore migration transaction failed.", e);
        }
    }

    private List<UserSetting> mergeSettingsWithCredentials(List<DocumentSnapshot> settingsDocs)
            throws InterruptedException, ExecutionException {

        if (CollectionUtils.isEmpty(settingsDocs)) {
            return Collections.emptyList();
        }

        String credentialsCollection = FirestoreCollectionNames.USER_CREDENTIALS;
        DocumentReference[] credentialRefs = settingsDocs.stream()
                .map(doc -> firestore.collection(credentialsCollection).document(doc.getId()))
                .toArray(DocumentReference[]::new);

        List<DocumentSnapshot> credentialsDocs = firestore.getAll(credentialRefs).get();
        Map<String, DocumentSnapshot> credentialsMap = credentialsDocs.stream()
                .collect(Collectors.toMap(DocumentSnapshot::getId, doc -> doc));

        return settingsDocs.stream().map(settingsDoc -> {
            DocumentSnapshot credentialsDoc = credentialsMap.get(settingsDoc.getId());
            return mapDocumentsToSetting(settingsDoc, credentialsDoc);
        }).collect(Collectors.toList());
    }

    private UserSetting mapDocumentsToSetting(DocumentSnapshot settingsDoc, DocumentSnapshot credentialsDoc) {
        Long createdAtLong = settingsDoc.getLong(FIELD_CREATED_AT);
        Instant createdAt = InstantUtil.toInstantOrNow(createdAtLong);
        Long updatedAtLong = settingsDoc.getLong(FIELD_UPDATED_AT);
        Instant updatedAt = updatedAtLong != null ? InstantUtil.toInstant(updatedAtLong) : createdAt;
        Boolean activeVal = settingsDoc.getBoolean(FIELD_IS_ACTIVE);
        boolean isActive = BooleanUtils.isTrue(activeVal);

        String googleEmail = null;
        String googleCalendarId = null;
        String googleRefreshToken = null;
        boolean isGoogleLinked = false;

        if (credentialsDoc != null && credentialsDoc.exists()) {
            googleEmail = credentialsDoc.getString(FIELD_GOOGLE_EMAIL);
            googleCalendarId = credentialsDoc.getString(FIELD_GOOGLE_CALENDAR_ID);
            String encryptedToken = credentialsDoc.getString(FIELD_ENCRYPTED_REFRESH_TOKEN);
            if (StringUtils.isNotBlank(encryptedToken)) {
                googleRefreshToken = encryptedToken;
                isGoogleLinked = true;
            }
        }

        return UserSetting.builder()
                .slackUserId(settingsDoc.getString(FIELD_SLACK_USER_ID))
                .email(googleEmail)
                .userName(settingsDoc.getString(FIELD_USER_NAME))
                .remindTime(settingsDoc.getString(FIELD_REMIND_TIME))
                .googleCalendarId(googleCalendarId)
                .active(isActive)
                .googleLinked(isGoogleLinked)
                .googleRefreshToken(googleRefreshToken)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
