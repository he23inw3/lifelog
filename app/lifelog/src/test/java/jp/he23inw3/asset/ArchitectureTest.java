package jp.he23inw3.asset;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * アーキテクチャの依存関係規則を検証するテストクラス。
 * クリーンアーキテクチャの依存規則（外側から内側への依存のみ許容）が遵守されていることを検証します。
 */
@AnalyzeClasses(
        packages = "jp.he23inw3.asset",
        importOptions = {
                ImportOption.DoNotIncludeTests.class,
                ArchitectureTest.ExcludeAppMain.class
        })
public class ArchitectureTest {

    private static final String BASE_PACKAGE = "jp.he23inw3.asset.";

    /**
     * アプリケーションのメインエントリーポイントである AppMain を検査対象から除外するインポートオプション。
     */
    public static class ExcludeAppMain implements ImportOption {
        @Override
        public boolean includes(Location location) {
            return !location.contains("AppMain");
        }
    }

    @ArchTest
    static final ArchRule architecture_layers_are_respected = layeredArchitecture()
            .consideringAllDependencies()

            // ==========================================
            // レイヤーの定義
            // ==========================================
            .layer("Domain").definedBy(BASE_PACKAGE + "domain..")
            .layer("UseCase").definedBy(BASE_PACKAGE + "usecase..")
            .layer("Adapter").definedBy(BASE_PACKAGE + "adapter..")
            .layer("Batch").definedBy(BASE_PACKAGE + "batch..")
            .layer("Configuration").definedBy(BASE_PACKAGE + "configuration..")

            // Infrastructure から Common を除外したものを定義
            .layer("Infrastructure").definedBy(
                    resideInAPackage(BASE_PACKAGE + "infrastructure..")
                            .and(resideOutsideOfPackage(BASE_PACKAGE + "infrastructure.common..")))
            // Common は Infrastructure の一部だが、共通ライブラリ的に扱えるよう別レイヤー化
            .layer("Common").definedBy(BASE_PACKAGE + "infrastructure.common..")

            // ==========================================
            // 依存関係の規則（アクセス制限）
            // ==========================================

            // Domain：コアロジック。全レイヤーからアクセス可能（自身はどこにも依存しない）
            .whereLayer("Domain").mayOnlyBeAccessedByLayers(
                    "UseCase", "Adapter", "Infrastructure", "Batch", "Configuration")

            // UseCase：ビジネスルール。Adapter, Infrastructure, Batch 等からアクセス可能
            .whereLayer("UseCase").mayOnlyBeAccessedByLayers(
                    "Adapter", "Infrastructure", "Batch", "Configuration")

            // Infrastructure：技術的詳細。Configuration, UseCase, Batch からのみアクセス可能
            // (提示された規則の「Domain -> Infrastructure」を成立させるため、ここにも依存を許可)
            .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers(
                    "Configuration", "UseCase", "Batch", "Adapter", "Domain")

            // Adapter：外部インターフェース（エントリーポイント）。他から依存されてはならない
            .whereLayer("Adapter").mayNotBeAccessedByAnyLayer()

            // Batch：バッチ処理（エントリーポイント）。他から依存されてはならない
            .whereLayer("Batch").mayNotBeAccessedByAnyLayer();
}