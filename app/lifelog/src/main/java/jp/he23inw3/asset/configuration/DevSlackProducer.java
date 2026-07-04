package jp.he23inw3.asset.configuration;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.slack.api.RequestConfigurator;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.auth.AuthTestResponse;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
@Alternative // 本番のProducerよりこちらを優先させる記述
@Priority(1)
@IfBuildProfile("dev") // クォーカス開発モード（%dev）の時だけ有効
public class DevSlackProducer {

    /**
     * dev プロファイル時に優先してインジェクションされる Slack インスタンスを生成します。
     * 
     * @return Slack API のスタブクライアント
     */
    @Produces
    @ApplicationScoped
    public Slack slack() {
        // メソッドや通信をMockito等でスタブ化した擬似インスタンスを返す
        Slack mockSlack = mock(Slack.class);
        try {
            MethodsClient mockMethods = mock(MethodsClient.class);
            AuthTestResponse mockResponse = mock(AuthTestResponse.class);

            when(mockSlack.methods(anyString())).thenReturn(mockMethods);
            when(mockMethods.authTest(any(RequestConfigurator.class))).thenReturn(mockResponse);
            when(mockResponse.isOk()).thenReturn(true);
        } catch (Exception e) {
            // ignore
        }
        return mockSlack;
    }
}
