package jp.he23inw3.asset.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.genai.Client;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import jp.he23inw3.asset.infrastructure.common.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@Alternative
@Priority(1)
@IfBuildProfile("dev")
@RequiredArgsConstructor
public class DevVertexAiProducer {

    private final LifeLogConfig config;

    /**
     * Google Gen AI クライアントを生成します（開発用）。
     * 
     * @return Gen AI クライアント
     */
    @Produces
    @ApplicationScoped
    public Client genAiClient() {
        // SDKに対して Vertex AI 接続モードとロケーションを明示的に強制する
        System.setProperty("GOOGLE_GENAI_USE_VERTEXAI", "true");
        System.setProperty("GOOGLE_CLOUD_LOCATION", config.gemini().location());

        // Quarkusエミュレータ設定による dummy-credentials.json がシステムプロパティを汚染しているのを防ぐため、
        // クライアント初期化の間だけ、一時的に GOOGLE_APPLICATION_CREDENTIALS を本物のADCパスに差し替えます。
        String originalCredsProperty = System.getProperty("GOOGLE_APPLICATION_CREDENTIALS");
        File adcFile = getAdcFile();

        if (adcFile != null && adcFile.exists()) {
            log.info(MessageHelper.getMessage("infra.devvertexai.creds.set", adcFile.getAbsolutePath()));
            System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", adcFile.getAbsolutePath());
        } else {
            log.warn(MessageHelper.getMessage("infra.devvertexai.creds.missing"));
        }

        try {
            Client.Builder builder = Client.builder()
                    .project(config.google().projectId())
                    .location(config.gemini().location())
                    .vertexAI(true);

            GoogleCredentials credentials = getRealAdcCredentials(adcFile);
            if (credentials != null) {
                log.info(MessageHelper.getMessage("infra.devvertexai.creds.loaded", config.gemini().location()));
                builder.credentials(credentials);
            } else {
                log.warn(MessageHelper.getMessage("infra.devvertexai.creds.load.failed.fallback"));
            }

            return builder.build();
        } finally {
            // 他のエミュレータ（Firestore / BigQuery）に影響を与えないよう、プロパティを元に戻す
            if (originalCredsProperty != null) {
                System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", originalCredsProperty);
            } else {
                System.clearProperty("GOOGLE_APPLICATION_CREDENTIALS");
            }
            log.info(MessageHelper.getMessage("infra.devvertexai.creds.restored"));
        }
    }

    private File getAdcFile() {
        String os = System.getProperty("os.name").toLowerCase();
        File adcFile = null;

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            log.info(MessageHelper.getMessage("infra.devvertexai.os.win", appData));
            if (appData != null) {
                adcFile = new File(appData, "gcloud/application_default_credentials.json");
            }
        } else {
            String home = System.getenv("HOME");
            log.info(MessageHelper.getMessage("infra.devvertexai.os.unix", home));
            if (home != null) {
                adcFile = new File(home, ".config/gcloud/application_default_credentials.json");
            }
        }
        return adcFile;
    }

    private GoogleCredentials getRealAdcCredentials(File adcFile) {
        if (adcFile != null) {
            log.info(MessageHelper.getMessage("infra.devvertexai.creds.searching", adcFile.getAbsolutePath()));
            if (adcFile.exists()) {
                try (FileInputStream fis = new FileInputStream(adcFile)) {
                    GoogleCredentials creds = GoogleCredentials.fromStream(fis);
                    log.info(MessageHelper.getMessage("infra.devvertexai.creds.parsed"));
                    return creds;
                } catch (IOException e) {
                    log.error(MessageHelper.getMessage("infra.devvertexai.creds.parse.error", adcFile.getAbsolutePath()), e);
                }
            } else {
                log.warn(MessageHelper.getMessage("infra.devvertexai.creds.notexist", adcFile.getAbsolutePath()));
            }
        } else {
            log.warn(MessageHelper.getMessage("infra.devvertexai.creds.path.unresolved"));
        }
        return null;
    }
}
