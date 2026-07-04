package jp.he23inw3.asset.configuration;

import com.google.cloud.NoCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;

/**
 * 開発プロファイル（dev）において、ローカルの BigQuery エミュレータに接続するための BigQuery クライアントプロデューサー。
 * <p>
 * Quarkus 拡張機能のデフォルトの BigQuery 接続設定を上書きし、認証なしでエミュレータにリダイレクトします。
 */
@Alternative
@Priority(1)
@ApplicationScoped
public class DevBigQueryProducer {

    /**
     * dev プロファイル時に優先してインジェクションされる BigQuery インスタンスを生成します。
     *
     * @return BigQuery エミュレータ接続用クライアント
     */
    @Produces
    @IfBuildProfile("dev")
    @Alternative
    @Priority(1)
    public BigQuery devBigQuery() {
        return BigQueryOptions.newBuilder()
                .setProjectId("lifelog-dev")
                .setHost("http://localhost:9050")
                .setCredentials(NoCredentials.getInstance())
                .build()
                .getService();
    }
}
