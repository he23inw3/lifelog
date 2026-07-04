# Service Account for Cloud Build executor
resource "google_service_account" "cloudbuild_sa" {
  account_id   = "${var.application_name}-cloudbuild-sa"
  display_name = "LifeLog Cloud Build execution Service Account"
  description  = "Service account used by Cloud Build to build Jib images and deploy Cloud Run resources"

  depends_on = [google_project_service.services]
}

# Bind IAM roles to Cloud Build Service Account (assigned after wait timer)
resource "google_project_iam_member" "cloudbuild_logging" {
  project = var.project_id
  role    = "roles/logging.logWriter"
  member  = "serviceAccount:${google_service_account.cloudbuild_sa.email}"

  depends_on = [time_sleep.wait_for_google]
}

resource "google_project_iam_member" "cloudbuild_ar_writer" {
  project = var.project_id
  role    = "roles/artifactregistry.writer"
  member  = "serviceAccount:${google_service_account.cloudbuild_sa.email}"

  depends_on = [time_sleep.wait_for_google]
}

resource "google_project_iam_member" "cloudbuild_run_admin" {
  project = var.project_id
  role    = "roles/run.admin"
  member  = "serviceAccount:${google_service_account.cloudbuild_sa.email}"

  depends_on = [time_sleep.wait_for_google]
}

# Allow Cloud Build SA to impersonate Cloud Run SA (required to deploy Cloud Run services and jobs)
resource "google_service_account_iam_member" "cloudbuild_act_as_cloudrun" {
  service_account_id = google_service_account.cloud_run_sa.name
  role               = "roles/iam.serviceAccountUser"
  member             = "serviceAccount:${google_service_account.cloudbuild_sa.email}"

  depends_on = [time_sleep.wait_for_google]
}

resource "google_project_iam_member" "cloudbuild_storage_admin" {
  project = var.project_id
  role    = "roles/storage.admin"
  member  = "serviceAccount:${google_service_account.cloudbuild_sa.email}"

  depends_on = [time_sleep.wait_for_google]
}
