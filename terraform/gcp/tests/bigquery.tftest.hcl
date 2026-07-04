# ─────────────────────────────────────────────────────────────
# BigQuery Dataset & Table — Unit Tests (plan only)
# ─────────────────────────────────────────────────────────────

variables {
  application_name   = "lifelog"
  project_id         = "test-project-id"
  region             = "asia-northeast1"
  environment        = "production"
  github_repo        = "he23inw3/lifelog"
  billing_account_id = "000000-000000-000000"
  notification_email = "test@example.com"
  container_image    = "us-docker.pkg.dev/cloudrun/container/hello"
  portal_cors_origin = "https://lifelog-portal.pages.dev"
  demo_cors_origin   = "https://lifelog-demo.pages.dev"
}

# ─────────────────────────────────────────────────────────────
# Dataset
# ─────────────────────────────────────────────────────────────

# BigQueryデータセットIDがアプリ名を含む正しい形式であること
run "bq_dataset_id_format" {
  command = plan

  assert {
    condition     = google_bigquery_dataset.lifelog_dataset.dataset_id == "lifelog_dataset"
    error_message = "BigQuery データセット ID は 'lifelog_dataset' であること"
  }
}

# BigQueryデータセットのロケーションが正しいこと
run "bq_dataset_location" {
  command = plan

  assert {
    condition     = google_bigquery_dataset.lifelog_dataset.location == "asia-northeast1"
    error_message = "BigQuery データセットのロケーションは 'asia-northeast1' であること"
  }
}

# BigQueryデータセットにテーブル有効期限が設定されていないこと
run "bq_dataset_expiration_null" {
  command = plan

  assert {
    condition     = google_bigquery_dataset.lifelog_dataset.default_table_expiration_ms == null
    error_message = "日報データは無期限保持のためテーブル有効期限を設定しないこと"
  }
}

# ─────────────────────────────────────────────────────────────
# Table
# ─────────────────────────────────────────────────────────────

# BigQueryテーブルIDが正しいこと
run "bq_table_id" {
  command = plan

  assert {
    condition     = google_bigquery_table.daily_logs.table_id == "daily_logs"
    error_message = "BigQuery テーブル ID は 'daily_logs' であること"
  }
}

# BigQueryテーブルが日付単位のパーティションをlog_dateカラムで設定していること
run "bq_table_partition_config" {
  command = plan

  assert {
    condition     = google_bigquery_table.daily_logs.time_partitioning[0].type == "DAY"
    error_message = "daily_logs テーブルのパーティションタイプは DAY であること"
  }

  assert {
    condition     = google_bigquery_table.daily_logs.time_partitioning[0].field == "log_date"
    error_message = "daily_logs テーブルは 'log_date' カラムでパーティションされること"
  }
}

# BigQueryテーブルのパーティションフィルターが必須であること
run "bq_table_partition_filter_required" {
  command = plan

  assert {
    condition     = google_bigquery_table.daily_logs.require_partition_filter == true
    error_message = "全件スキャンを防ぐためパーティションフィルターを必須にすること"
  }
}

# BigQueryテーブルのクラスタリングカラムが正しいこと
run "bq_table_clustering_columns" {
  command = plan

  assert {
    condition = toset(google_bigquery_table.daily_logs.clustering) == toset([
      "slack_user_id",
      "sentiment",
      "is_holiday",
    ])
    error_message = "daily_logs のクラスタリングカラムは slack_user_id, sentiment, is_holiday であること"
  }
}

# BigQueryテーブルの削除保護が無効であること
run "bq_table_deletion_protection_disabled" {
  command = plan

  # 再プロビジョニングの柔軟性のため削除保護は意図的に無効
  assert {
    condition     = google_bigquery_table.daily_logs.deletion_protection == false
    error_message = "再プロビジョニングを可能にするため daily_logs の deletion_protection は false であること"
  }
}
