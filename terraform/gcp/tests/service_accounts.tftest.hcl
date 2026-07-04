# ─────────────────────────────────────────────────────────────
# Service Accounts & IAM — Unit Tests (plan only)
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
# Cloud Run Service Account
# ─────────────────────────────────────────────────────────────

# Cloud_Run実行用サービスアカウントのIDがアプリ名を含む正しい形式であること
run "sa_cloud_run_id" {
  command = plan

  assert {
    condition     = google_service_account.cloud_run_sa.account_id == "lifelog-run-sa"
    error_message = "Cloud Run SA の account_id は 'lifelog-run-sa' であること"
  }
}

# ─────────────────────────────────────────────────────────────
# Cloud Scheduler Service Account
# ─────────────────────────────────────────────────────────────

# Cloud_Scheduler用サービスアカウントのIDがアプリ名を含む正しい形式であること
run "sa_scheduler_id" {
  command = plan

  assert {
    condition     = google_service_account.scheduler_sa.account_id == "lifelog-scheduler-sa"
    error_message = "Scheduler SA の account_id は 'lifelog-scheduler-sa' であること"
  }
}

# ─────────────────────────────────────────────────────────────
# GitHub Actions Service Account
# ─────────────────────────────────────────────────────────────

# GitHub_Actions用サービスアカウントのIDがアプリ名を含む正しい形式であること
run "sa_github_actions_id" {
  command = plan

  assert {
    condition     = google_service_account.github_actions_sa.account_id == "lifelog-github-actions-sa"
    error_message = "GitHub Actions SA の account_id は 'lifelog-github-actions-sa' であること"
  }
}

# ─────────────────────────────────────────────────────────────
# IAM Bindings — Cloud Run SA roles
# ─────────────────────────────────────────────────────────────

# Cloud_Run_SAがFirestoreアクセス用のdatastore_userロールを持つこと
run "sa_cloud_run_firestore_role" {
  command = plan

  assert {
    condition     = google_project_iam_member.cloud_run_firestore_user.role == "roles/datastore.user"
    error_message = "Cloud Run SA は Firestore アクセスのため roles/datastore.user を持つこと"
  }
}

# Cloud_Run_SAがBigQueryジョブ実行用のbigquery_jobUserロールを持つこと
run "sa_cloud_run_bq_job_role" {
  command = plan

  assert {
    condition     = google_project_iam_member.cloud_run_bq_job_user.role == "roles/bigquery.jobUser"
    error_message = "Cloud Run SA は roles/bigquery.jobUser を持つこと"
  }
}

# Cloud_Run_SAが日報データ書き込み用のbigquery_dataEditorロールを持つこと
run "sa_cloud_run_bq_data_editor_role" {
  command = plan

  assert {
    condition     = google_project_iam_member.cloud_run_bq_data_editor.role == "roles/bigquery.dataEditor"
    error_message = "Cloud Run SA は日報データ書き込みのため roles/bigquery.dataEditor を持つこと"
  }
}

# Cloud_Run_SAがVertex_AI_Gemini呼び出し用のaiplatform_userロールを持つこと
run "sa_cloud_run_aiplatform_role" {
  command = plan

  assert {
    condition     = google_project_iam_member.cloud_run_vertex_user.role == "roles/aiplatform.user"
    error_message = "Cloud Run SA は Vertex AI / Gemini アクセスのため roles/aiplatform.user を持つこと"
  }
}

# ─────────────────────────────────────────────────────────────
# IAM Bindings — Scheduler SA roles
# ─────────────────────────────────────────────────────────────

# Scheduler_SAがCloud_Runジョブのトリガーにrun.jobs.executorロールを持つこと
run "sa_scheduler_run_developer_role" {
  command = plan

  assert {
    condition     = google_project_iam_member.scheduler_run_developer.role == "roles/run.jobs.executor"
    error_message = "Scheduler SA は Cloud Run Jobs トリガーのため roles/run.jobs.executor を持つこと"
  }
}

# ─────────────────────────────────────────────────────────────
# IAM Bindings — GitHub Actions SA roles
# ─────────────────────────────────────────────────────────────

# GitHub_Actions_SAがコンテナイメージプッシュ用のartifactregistry_writerロールを持つこと
run "sa_github_actions_ar_writer_role" {
  command = plan

  assert {
    condition     = google_project_iam_member.github_actions_ar_writer.role == "roles/artifactregistry.writer"
    error_message = "GitHub Actions SA はコンテナイメージのプッシュのため roles/artifactregistry.writer を持つこと"
  }
}

# GitHub_Actions_SAがCloud_Runデプロイ用のrun_adminロールを持つこと
run "sa_github_actions_run_admin_role" {
  command = plan

  assert {
    condition     = google_project_iam_member.github_actions_run_admin.role == "roles/run.admin"
    error_message = "GitHub Actions SA は Cloud Run サービスデプロイのため roles/run.admin を持つこと"
  }
}
