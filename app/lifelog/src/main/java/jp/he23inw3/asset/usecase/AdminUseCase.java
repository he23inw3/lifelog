package jp.he23inw3.asset.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.Optional;
import jp.he23inw3.asset.domain.constant.UserMessageConstants;
import jp.he23inw3.asset.domain.exception.ForbiddenException;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.model.AdminUser;
import jp.he23inw3.asset.domain.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;

/**
 * 管理者情報の取得・保存・更新に関するビジネスロジックを制御するユースケースクラス。
 * <p>
 * 新規作成時は作成者/作成日時を設定し、更新時は作成者情報を維持したまま更新者/更新日時を設定します。
 */
@ApplicationScoped
@RequiredArgsConstructor
public class AdminUseCase {

    private final AdminUserRepository adminUserRepository;

    /**
     * 指定されたメールアドレスに対応する管理者ユーザー情報を取得します。
     *
     * @param email メールアドレス
     * @return 管理者情報のドメインモデル
     * @throws ResourceNotFoundException 指定された管理者が存在しない場合
     */
    public AdminUser getAdmin(String email) {
        return adminUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found for " + email));
    }

    /**
     * 管理者情報を保存または更新します。
     *
     * @param newAdmin 保存または更新対象の管理者ユーザー情報
     * @param operatorEmail 処理を実行したオペレーター（操作者）のメールアドレス
     * @return 保存完了後の管理者ユーザー情報
     */
    public AdminUser saveAdmin(AdminUser newAdmin, String operatorEmail) {
        // 通常モード（管理者が1名以上存在）かつ、操作を実行したユーザーが有効な管理者でない場合は権限エラー
        if (!isAdminCollectionEmpty() && !isActiveAdmin(operatorEmail)) {
            throw new ForbiddenException(UserMessageConstants.AUTH_FORBIDDEN);
        }

        Optional<AdminUser> existingOpt = adminUserRepository.findByEmail(newAdmin.getEmail());
        AdminUser toSave;

        if (existingOpt.isEmpty()) {
            toSave = buildNewAdminUser(newAdmin, operatorEmail);
        } else {
            toSave = buildUpdatedAdminUser(newAdmin, existingOpt.get(), operatorEmail);
        }

        adminUserRepository.save(toSave);
        return toSave;
    }

    /**
     * 管理者コレクションが空であるかどうかを判定します。
     *
     * @return 管理者が1人も存在しない場合は {@code true}、それ以外は {@code false}
     */
    public boolean isAdminCollectionEmpty() {
        return adminUserRepository.isEmpty();
    }

    /**
     * 指定されたメールアドレスのユーザーが有効な管理者であるかを判定します。
     *
     * @param email 判定対象のメールアドレス
     * @return 有効な管理者の場合は {@code true}、それ以外は {@code false}
     */
    public boolean isActiveAdmin(String email) {
        return adminUserRepository.findByEmail(email)
                .map(AdminUser::isActive)
                .orElse(false);
    }

    /**
     * 新規登録用の管理者ドメインモデルを構築します。
     *
     * @param newAdmin 基本情報が格納された管理者オブジェクト
     * @param operatorEmail 登録操作を実行したオペレーターのメールアドレス
     * @return 監査情報（作成者、作成日時等）が追加された AdminUser オブジェクト
     */
    private AdminUser buildNewAdminUser(AdminUser newAdmin, String operatorEmail) {
        Instant now = Instant.now();
        return newAdmin.toBuilder()
                .createdBy(operatorEmail)
                .createdAt(now)
                .updatedBy(operatorEmail)
                .updatedAt(now)
                .build();
    }

    /**
     * 更新用の管理者ドメインモデルを構築します。
     *
     * @param newAdmin 更新データが格納された管理者オブジェクト
     * @param existing 既存の登録済み管理者オブジェクト（作成者情報を引き継ぐため）
     * @param operatorEmail 更新操作を実行したオペレーターのメールアドレス
     * @return 既存の作成者情報を保護しつつ、新しい更新監査情報が設定された AdminUser オブジェクト
     */
    private AdminUser buildUpdatedAdminUser(AdminUser newAdmin, AdminUser existing, String operatorEmail) {
        return newAdmin.toBuilder()
                .createdBy(existing.getCreatedBy())
                .createdAt(existing.getCreatedAt())
                .updatedBy(operatorEmail)
                .updatedAt(Instant.now())
                .build();
    }
}
