package jp.he23inw3.asset.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.genai.Client;
import com.google.genai.types.Candidate;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import com.google.genai.types.UsageMetadata;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
@RegisterForReflection(
        targets = {
                GenerateContentConfig.class,
                GenerateContentConfig.Builder.class,
                GenerateContentResponse.class,
                Candidate.class,
                Content.class,
                Part.class,
                UsageMetadata.class,
                Schema.class,
                Schema.Builder.class,
                Type.class
        })
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
