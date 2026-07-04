package jp.he23inw3.asset.adapter.interceptor;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.util.Optional;
import jp.he23inw3.asset.adapter.constant.ApiPath;
import jp.he23inw3.asset.adapter.context.ApiContext;
import jp.he23inw3.asset.configuration.LifeLogConfig;
import jp.he23inw3.asset.domain.constant.UserMessageConstants;
import jp.he23inw3.asset.domain.model.AdminUser;
import jp.he23inw3.asset.domain.repository.AdminUserRepository;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * GCP IAM 連携用 OIDC トークンバリデータおよび管理者認可フィルター。
 * <p>
 * OIDC トークンの存在確認、有効なメールアドレスの抽出、および Firestore の管理者リストとの照合を行います。
 * 管理者が1件も登録されていない場合は、初回のブートストラップ登録のみ許可します。
 */
@Slf4j
@Provider
@RequiredArgsConstructor
public class AuthFilter implements ContainerRequestFilter {

    private static final String SLACK_EVENTS_PATH = ApiPath.SLACK_EVENTS;
    private static final String USERS_PATH = ApiPath.USERS_BASE;
    private static final String DEMO_PATH = "/api/v1/demo";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ApiContext apiContext;

    private final JsonWebToken jwt;

    private final AdminUserRepository adminUserRepository;

    private final jakarta.inject.Provider<LifeLogConfig> configProvider;

    /**
     * API リクエストのインターセプト・フィルタリング処理。
     *
     * @param requestContext
     *            リクエストコンテキスト
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        // OPTIONS (Preflight) リクエストはバイパス
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        String path = requestContext.getUriInfo().getPath();
        if (path != null && !path.startsWith("/")) {
            path = "/" + path;
        }

        // パスバイパス判定
        if (isBypassedPath(path)) {
            return;
        }

        // デモモードバイパス（/api/v1/users/** および /api/v1/demo/**）
        if (configProvider.get().demo().enabled() && isDemoBypassedPath(path)) {
            apiContext.setAuthenticatedUserId(configProvider.get().demo().userEmail());
            log.debug("[DEMO] デモモードバイパス: path={}, user={}", path, configProvider.get().demo().userEmail());
            return;
        }

        // Authorizationヘッダーチェック
        String authHeader = requestContext.getHeaderString("Authorization");
        if (!isValidAuthHeader(authHeader)) {
            log.warn(MessageHelper.getMessage("adapter.auth.warn.noheader", path));
            abortWithStatus(requestContext, Response.Status.UNAUTHORIZED, UserMessageConstants.AUTH_UNAUTHORIZED);
            return;
        }

        // トークンからのメールアドレス抽出
        String email = getClaimEmail(jwt);
        if (email == null) {
            log.warn(MessageHelper.getMessage("adapter.auth.warn.missingemail"));
            abortWithStatus(requestContext, Response.Status.UNAUTHORIZED, UserMessageConstants.AUTH_UNAUTHORIZED);
            return;
        }

        // 管理者認可チェック
        if (isAdminPath(path)) {
            if (!hasAdminPermission(email)) {
                if (isBootstrapAllowed(email)) {
                    log.info(MessageHelper.getMessage("adapter.auth.info.bootstrap"));
                } else {
                    log.warn(MessageHelper.getMessage("adapter.auth.warn.noadmin"));
                    abortWithStatus(requestContext, Response.Status.FORBIDDEN, UserMessageConstants.AUTH_FORBIDDEN);
                    return;
                }
            }
        }

        // 正常：APIコンテキストに認証されたユーザーIDを設定
        apiContext.setAuthenticatedUserId(email);
        log.debug(MessageHelper.getMessage("adapter.auth.debug.verify", path));
    }

    /**
     * 認証が不要なバイパス対象パスであるかを判定します。
     *
     * @param path
     *            リクエストパス
     * @return バイパス対象の場合は {@code true}、それ以外は {@code false}
     */
    private boolean isBypassedPath(String path) {
        return path.startsWith(SLACK_EVENTS_PATH) || path.startsWith(ApiPath.SLACK_COMMANDS)
                || path.startsWith("/swagger-ui") || path.startsWith("/q/swagger-ui") || path.startsWith("/q/openapi")
                || path.startsWith("/api/v1/auth/google");
    }

    /**
     * デモモード時に認証をバイパスする対象パスであるかを判定します。 管理者エンドポイント（/api/v1/admin/**）はデモモードでも OIDC 必須。
     *
     * @param path
     *            リクエストパス
     * @return デモモードバイパス対象の場合は {@code true}
     */
    private boolean isDemoBypassedPath(String path) {
        return path.startsWith(USERS_PATH) || path.startsWith(DEMO_PATH);
    }

    /**
     * リクエストパスが管理者向けエンドポイントであるかを判定します。
     *
     * @param path
     *            リクエストパス
     * @return 管理者向けエンドポイントの場合は {@code true}、それ以外は {@code false}
     */
    private boolean isAdminPath(String path) {
        return path.startsWith(ApiPath.ADMIN_BASE);
    }

    /**
     * Authorization ヘッダー値が有効な Bearer フォーマットであるかを判定します。
     *
     * @param authHeader
     *            Authorization ヘッダーの値
     * @return 有効な場合は {@code true}、それ以外は {@code false}
     */
    private boolean isValidAuthHeader(String authHeader) {
        return authHeader != null && authHeader.startsWith(BEARER_PREFIX);
    }

    /**
     * JWT トークンから電子メールアドレスを取得します。
     *
     * @param token
     *            JWT オブジェクト
     * @return 電子メールアドレス、取得できない場合は {@code null}
     */
    private String getClaimEmail(JsonWebToken token) {
        if (token == null) {
            return null;
        }
        return token.getClaim("email");
    }

    /**
     * 対象のメールアドレスを持つユーザーが管理者権限（アクティブな管理者）を有しているかを判定します。
     *
     * @param email
     *            判定対象のメールアドレス
     * @return 管理者権限を持つ場合は {@code true}、それ以外は {@code false}
     */
    private boolean hasAdminPermission(String email) {
        Optional<AdminUser> adminOpt = adminUserRepository.findByEmail(email);
        return adminOpt.isPresent() && adminOpt.get().isActive();
    }

    /**
     * 管理者が1人も存在しない場合、初回の管理者登録として処理の続行を許可（ブートストラップモード）します。
     *
     * @param email
     *            登録操作を実行しようとしているメールアドレス
     * @return ブートストラップとして許可する場合は {@code true}、それ以外は {@code false}
     */
    private boolean isBootstrapAllowed(String email) {
        if (!adminUserRepository.isEmpty()) {
            return false;
        }
        // 管理者が0人の場合、許可されたメールアドレスと一致するかチェック
        return configProvider.get().bootstrap().allowedEmail()
                .map(allowedEmail -> allowedEmail.equals(email))
                .orElse(false);
    }

    /**
     * リクエストの処理を中断し、指定された HTTP ステータスとエラーメッセージを返却します。
     *
     * @param requestContext
     *            リクエストコンテキスト
     * @param status
     *            返却する HTTP ステータス
     * @param errorMessage
     *            エラーレスポンスに含めるメッセージ
     */
    private void abortWithStatus(ContainerRequestContext requestContext, Response.Status status, String errorMessage) {
        requestContext.abortWith(Response.status(status).entity("{\"error\":\"" + errorMessage + "\"}").build());
    }
}
