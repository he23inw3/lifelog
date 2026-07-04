package jp.he23inw3.asset.adapter.rest;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jp.he23inw3.asset.adapter.constant.ApiPath;
import jp.he23inw3.asset.adapter.constant.ApiTag;
import jp.he23inw3.asset.adapter.context.ApiContext;
import jp.he23inw3.asset.adapter.dto.UserRegistrationRequest;
import jp.he23inw3.asset.adapter.dto.UserSettingResponse;
import jp.he23inw3.asset.adapter.mapper.UserSettingMapper;
import jp.he23inw3.asset.domain.exception.InvalidRegistrationException;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.usecase.SlackLinkageUseCase;
import jp.he23inw3.asset.usecase.UserSettingUseCase;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * 利用者初回登録エンドポイントを提供する REST リソースクラス。
 * <p>
 * Google OAuth 後またはデモモードで利用者を新規登録します。 OIDC メールアドレスを {@link ApiContext} から取得し、{@link UserSetting#email} に紐付けます。
 */
@Path(ApiPath.USERS_BASE)
@Tag(name = ApiTag.USER)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class UserRegistrationResource {

    private final ApiContext apiContext;

    private final UserSettingUseCase userSettingUseCase;

    private final UserSettingMapper userSettingMapper;

    private final SlackLinkageUseCase slackLinkageUseCase;

    /**
     * Google OAuth 後またはデモモードで利用者を新規登録します。
     * <p>
     * OIDC メールアドレスを取得し、{@link UserSetting#email} に結び付けて保存します。
     *
     * @param request 利用者初回登録データを含むリクエスト DTO
     * @return 登録後のユーザー設定レスポンス DTO
     */
    @POST
    @Path("/register")
    @Operation(operationId = "BE-API102", summary = "利用者初回登録", description = "Google OAuth後またはデモモードで利用者を新規登録します")
    public UserSettingResponse register(@Valid UserRegistrationRequest request) {
        String email = apiContext.getAuthenticatedUserId();

        String slackUserId = request.getSlackUserId();
        String slackToken = request.getSlackToken();

        // slackToken が指定されている場合は、トークンから slackUserId を自動解決する
        if (StringUtils.isNotBlank(slackToken)) {
            slackUserId = slackLinkageUseCase.verifyAndConsumeToken(slackToken);
        }

        // slackUserId が解決されなかった（または空）の場合はエラー
        if (StringUtils.isBlank(slackUserId)) {
            throw new InvalidRegistrationException("Slack ユーザー ID または Slack 連携トークンは必須です。");
        }

        // 解決した slackUserId をリクエスト DTO に明示的に設定
        request.setSlackUserId(slackUserId);

        UserSetting setting = userSettingMapper.toDomain(request, email);
        UserSetting saved = userSettingUseCase.registerUser(setting);
        return userSettingMapper.toResponse(saved);
    }
}
