package jp.he23inw3.asset.configuration;

import com.google.genai.Client;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class VertexAiProducer {

    private final LifeLogConfig config;

    /**
     * Google Gen AI クライアントを生成します（本番用）。
     * 
     * @return Gen AI クライアント
     */
    @Produces
    @ApplicationScoped
    @IfBuildProfile("prod")
    public Client genAiClient() {
        return Client.builder()
                .project(config.google().projectId())
                .location(config.gemini().location())
                .vertexAI(true)
                .build();
    }
}
