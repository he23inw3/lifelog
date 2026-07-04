# Service Account for Cloud Run API Service and Cloud Run Jobs
resource "google_service_account" "cloud_run_sa" {
  account_id   = "${var.application_name}-run-sa"
  display_name = "LifeLog Cloud Run execution Service Account"
  description  = "Service account used by Cloud Run service and Jobs to access GCP resources"

  depends_on = [google_project_service.services]
}

# Service Account for Cloud Scheduler triggers
resource "google_service_account" "scheduler_sa" {
  account_id   = "${var.application_name}-scheduler-sa"
  display_name = "LifeLog Cloud Scheduler trigger Service Account"
  description  = "Service account used by Cloud Scheduler to securely run Cloud Run Jobs"

  depends_on = [google_project_service.services]
}

# Time delay to wait for GCP Services APIs and Base Service Accounts to propagate
resource "time_sleep" "wait_for_google" {
  depends_on = [
    google_project_service.services,
    google_artifact_registry_repository.lifelog_repo,
    google_bigquery_dataset.lifelog_dataset,
    google_bigquery_table.daily_logs,
    google_firestore_database.database,
    google_iam_workload_identity_pool.github_pool,
    google_service_account.cloud_run_sa,
    google_service_account.scheduler_sa,
    google_service_account.github_actions_sa,
    google_service_account.cloudbuild_sa,
  ]
  create_duration = "180s"
}

# IAM Role bindings for Cloud Run SA (assigned after the wait timer)
resource "google_project_iam_member" "cloud_run_firestore_user" {
  project = var.project_id
  role    = "roles/datastore.user"
  member  = "serviceAccount:${google_service_account.cloud_run_sa.email}"

  depends_on = [time_sleep.wait_for_google]
}

resource "google_project_iam_member" "cloud_run_bq_job_user" {
  project = var.project_id
  role    = "roles/bigquery.jobUser"
  member  = "serviceAccount:${google_service_account.cloud_run_sa.email}"

  depends_on = [time_sleep.wait_for_google]
}

resource "google_project_iam_member" "cloud_run_bq_data_editor" {
  project = var.project_id
  role    = "roles/bigquery.dataEditor"
  member  = "serviceAccount:${google_service_account.cloud_run_sa.email}"

  depends_on = [time_sleep.wait_for_google]
}

resource "google_project_iam_member" "cloud_run_vertex_user" {
  project = var.project_id
  role    = "roles/aiplatform.user"
  member  = "serviceAccount:${google_service_account.cloud_run_sa.email}"

  depends_on = [time_sleep.wait_for_google]
}

resource "google_secret_manager_secret_iam_member" "cloud_run_secret_accessor" {
  for_each = toset([
    google_secret_manager_secret.slack_signing_secret.secret_id,
    google_secret_manager_secret.slack_bot_token.secret_id,
    google_secret_manager_secret.oidc_client_id.secret_id,
    google_secret_manager_secret.crypto_key.secret_id,
  ])
  secret_id = each.key
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.cloud_run_sa.email}"

  depends_on = [time_sleep.wait_for_google]
}

# IAM ロールバインディング（Cloud Scheduler SA）— wait timer 後に付与
# roles/run.jobsExecutor: Cloud Run Job の HTTP 実行に必要な最小権限
resource "google_project_iam_member" "scheduler_run_developer" {
  project = var.project_id
  role    = "roles/run.jobsExecutor"
  member  = "serviceAccount:${google_service_account.scheduler_sa.email}"

  depends_on = [time_sleep.wait_for_google]
}
