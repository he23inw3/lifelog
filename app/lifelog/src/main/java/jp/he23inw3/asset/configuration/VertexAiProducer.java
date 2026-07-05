package jp.he23inw3.asset.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.genai.Client;
import com.google.genai.types.Blob;
import com.google.genai.types.Candidate;
import com.google.genai.types.CitationMetadata;
import com.google.genai.types.CodeExecutionResult;
import com.google.genai.types.Content;
import com.google.genai.types.ExecutableCode;
import com.google.genai.types.FileData;
import com.google.genai.types.FinishReason;
import com.google.genai.types.FunctionCall;
import com.google.genai.types.FunctionResponse;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentParameters;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentResponsePromptFeedback;
import com.google.genai.types.GenerateContentResponseUsageMetadata;
import com.google.genai.types.GroundingMetadata;
import com.google.genai.types.Part;
import com.google.genai.types.SafetyRating;
import com.google.genai.types.SafetySetting;
import com.google.genai.types.Schema;
import com.google.genai.types.Tool;
import com.google.genai.types.ToolConfig;
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
                Blob.class,
                Candidate.class,
                Candidate.Builder.class,
                CitationMetadata.class,
                CodeExecutionResult.class,
                Content.class,
                Content.Builder.class,
                ExecutableCode.class,
                FileData.class,
                FinishReason.class,
                FinishReason.Known.class,
                FunctionCall.class,
                FunctionResponse.class,
                GenerateContentConfig.class,
                GenerateContentConfig.Builder.class,
                GenerateContentParameters.class,
                GenerateContentParameters.Builder.class,
                GenerateContentResponse.class,
                GenerateContentResponse.Builder.class,
                GenerateContentResponsePromptFeedback.class,
                GenerateContentResponseUsageMetadata.class,
                GroundingMetadata.class,
                Part.class,
                Part.Builder.class,
                SafetyRating.class,
                SafetySetting.class,
                Schema.class,
                Schema.Builder.class,
                Tool.class,
                ToolConfig.class,
                Type.class,
                Type.Known.class,
                UsageMetadata.class,
                UsageMetadata.Builder.class
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
