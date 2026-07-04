package jp.he23inw3.asset.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

/**
 * SmallRye OpenAPI の全体メタデータ設定。
 * <p>
 * Swagger UI は {@code /swagger-ui} でアクセス可能。 OpenAPI JSON は {@code /q/openapi} で取得可能。
 */
@OpenAPIDefinition(
        info = @Info(
                title = "LifeLog API",
                version = "1.0.0",
                description = "ライフログシステム - AI対話型日報・日記ツール",
                contact = @Contact(name = "he23inw3")),
        servers = {
                @Server(url = "http://localhost:5000", description = "ローカル開発")
        },
        security = {
                @SecurityRequirement(name = "BearerAuth")
        })
@SecurityScheme(
        securitySchemeName = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "事前に取得した Google OIDC ID トークンをセットしてください。")
@ApplicationScoped
public class OpenApiConfig {
    // アノテーションのみで設定完結。追加の Bean 定義が必要な場合はここに追記する。
}
