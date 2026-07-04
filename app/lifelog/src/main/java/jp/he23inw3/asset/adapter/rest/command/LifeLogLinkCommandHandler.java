package jp.he23inw3.asset.adapter.rest.command;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import jp.he23inw3.asset.domain.exception.LifeLogException;
import jp.he23inw3.asset.usecase.SlackLinkageUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * "/lifelog-link" コマンドを処理し、アカウント連携URLを返却するハンドラークラス。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class LifeLogLinkCommandHandler implements SlackCommandHandler {

    private final SlackLinkageUseCase slackLinkageUseCase;

    @Override
    public boolean canHandle(String command) {
        return "/lifelog-link".equals(command);
    }

    @Override
    public Response handle(String command, String userId, String userName) {
        if (StringUtils.isBlank(userId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "user_id is required")).build();
        }

        try {
            String linkageUrl = slackLinkageUseCase.generateLinkageUrl(userId);

            String responseMessage = String.format("🔗 *<%s|this link> から、LifeLogアカウントとの連携を行ってください。 (有効期限は10分です)*", linkageUrl);

            return Response.ok(Map.of("response_type", "ephemeral", "text", responseMessage)).build();

        } catch (Exception e) {
            log.error("Failed to generate linkage token during slash command processing", e);
            throw new LifeLogException("Failed to process slash command token generation", e);
        }
    }
}
