package jp.he23inw3.asset.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import jp.he23inw3.asset.domain.model.Session;
import jp.he23inw3.asset.domain.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;

/**
 * 対話セッションのユースケース。
 * <p>
 * 聞き返しフローの途中状態（Firestore {@code user_sessions}）に関する業務処理を担う。
 */
@ApplicationScoped
@RequiredArgsConstructor
public class SessionUseCase {

    private final UserSessionRepository userSessionRepository;

    /**
     * 指定ユーザーの対話セッションを強制リセットする。
     *
     * @param slackUserId リセット対象の Slack ユーザー ID
     */
    public void resetSession(String slackUserId) {
        userSessionRepository.delete(slackUserId);
    }

    /**
     * すべての対話セッション一覧を取得します。
     *
     * @return すべての対話セッションのリスト
     */
    public List<Session> getAllSessions() {
        return userSessionRepository.findAll();
    }
}
