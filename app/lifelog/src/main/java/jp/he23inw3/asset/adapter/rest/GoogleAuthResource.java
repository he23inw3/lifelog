package jp.he23inw3.asset.adapter.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import jp.he23inw3.asset.usecase.GoogleAuthUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Google OAuth 2.0 連携のためのエンドポイントを提供する REST リソースクラス。
 */
@Slf4j
@Path("/api/v1/auth/google")
@Tag(name = "Auth")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@ApplicationScoped
public class GoogleAuthResource {

    private final GoogleAuthUseCase googleAuthUseCase;

    /**
     * Google OAuth 同意画面へのリダイレクトを処理します。
     *
     * @param email 連携対象 of target email
     * @return 同意画面へのリダイレクト応答
     */
    @GET
    @Path("/login")
    @Operation(operationId = "BE-API112", summary = "Google OAuth ログイン遷移", description = "Google の OAuth 2.0 認証画面へリダイレクトします。")
    public Response login(@QueryParam("email") String email) {
        String redirectUrl = googleAuthUseCase.getLoginRedirectUrl(email);
        return Response.seeOther(URI.create(redirectUrl)).build();
    }

    /**
     * Google OAuth からのリダイレクトコールバックを処理します。
     *
     * @param state 暗号化された LifeLog のユーザーメールアドレス
     * @param code 認可コード
     * @param error エラーメッセージ（拒否された場合など）
     * @return 設定画面へのリダイレクト応答
     */
    @GET
    @Path("/callback")
    @Operation(operationId = "BE-API113", summary = "Google OAuth コールバック", description = "Google OAuth 認可コードを受け取り、トークンと交換して保存します。")
    public Response callback(@QueryParam("state") String state, @QueryParam("code") String code,
            @QueryParam("error") String error) {
        String redirectUrl = googleAuthUseCase.handleCallback(state, code, error);
        return Response.seeOther(URI.create(redirectUrl)).build();
    }
}
