package jp.he23inw3.asset.configuration;

import com.slack.api.Slack;
import com.slack.api.SlackConfig;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class SlackProducer {

    /**
     * prod プロファイルの場合に Slack クライアントを生成します。
     * 
     * @return Slack クライアント
     */
    @Produces
    @ApplicationScoped
    @IfBuildProfile("prod")
    public Slack slack() {
        SlackConfig slackConfig = new SlackConfig();
        return Slack.getInstance(slackConfig);
    }
}
