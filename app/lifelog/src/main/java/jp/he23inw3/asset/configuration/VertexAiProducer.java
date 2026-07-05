package jp.he23inw3.asset.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.genai.Client;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.io.IOException;
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
        try {
            return Client.builder()
                    .project(config.google().projectId())
                    .location(config.gemini().location())
                    .vertexAI(true)
                    .credentials(GoogleCredentials.getApplicationDefault())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Application Default Credentials for Gemini client", e);
        }
    }
}
