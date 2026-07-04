# Workload Identity Pool for GitHub Actions
resource "google_iam_workload_identity_pool" "github_pool" {
  workload_identity_pool_id = "${var.application_name}-github-pool"
  display_name              = "GitHub Actions Pool"
  description               = "Workload Identity Pool for GitHub Actions integration"

  depends_on = [google_project_service.services]
}

# Workload Identity Provider for GitHub Actions OIDC
resource "google_iam_workload_identity_pool_provider" "github_provider" {
  workload_identity_pool_id          = google_iam_workload_identity_pool.github_pool.workload_identity_pool_id
  workload_identity_pool_provider_id = "${var.application_name}-github-provider"
  display_name                       = "GitHub Actions Provider"

  attribute_mapping = {
    "google.subject"             = "assertion.sub"
    "attribute.actor"            = "assertion.actor"
    "attribute.repository"       = "assertion.repository"
    "attribute.repository_owner" = "assertion.repository_owner"
  }

  # Restrict access only to the specified GitHub repository to secure the provider
  attribute_condition = "assertion.repository == '${var.github_repo}'"

  oidc {
    issuer_uri = "https://token.actions.githubusercontent.com"
  }
}

# Service Account dedicated for GitHub Actions deployments
resource "google_service_account" "github_actions_sa" {
  account_id   = "${var.application_name}-github-actions-sa"
  display_name = "GitHub Actions Deployment SA"
  description  = "Service account used by GitHub Actions to build and deploy LifeLog"

  depends_on = [google_project_service.services]
}

# Allow GitHub Actions repository to assume the Service Account via OIDC after wait timer
resource "google_service_account_iam_member" "github_actions_oidc" {
  service_account_id = google_service_account.github_actions_sa.name
  role               = "roles/iam.workloadIdentityUser"
  member             = "principalSet://iam.googleapis.com/${google_iam_workload_identity_pool.github_pool.name}/attribute.repository/${var.github_repo}"

  depends_on = [time_sleep.wait_for_google]
}

# Bind IAM roles to the GitHub Actions Service Account for deployment operations after wait timer
resource "google_project_iam_member" "github_actions_ar_writer" {
  project = var.project_id
  role    = "roles/artifactregistry.writer"
  member  = "serviceAccount:${google_service_account.github_actions_sa.email}"

  depends_on = [time_sleep.wait_for_google]
}

resource "google_project_iam_member" "github_actions_run_admin" {
  project = var.project_id
  role    = "roles/run.admin"
  member  = "serviceAccount:${google_service_account.github_actions_sa.email}"

  depends_on = [time_sleep.wait_for_google]
}

# Allow GitHub Actions SA to impersonate Cloud Run SA (required to deploy Cloud Run services and jobs)
resource "google_service_account_iam_member" "github_actions_act_as_cloudrun" {
  service_account_id = google_service_account.cloud_run_sa.name
  role               = "roles/iam.serviceAccountUser"
  member             = "serviceAccount:${google_service_account.github_actions_sa.email}"

  depends_on = [time_sleep.wait_for_google]
}

# Allow GitHub Actions SA to impersonate Cloud Scheduler SA (required to deploy Cloud Scheduler jobs)
resource "google_service_account_iam_member" "github_actions_act_as_scheduler" {
  service_account_id = google_service_account.scheduler_sa.name
  role               = "roles/iam.serviceAccountUser"
  member             = "serviceAccount:${google_service_account.github_actions_sa.email}"

  depends_on = [time_sleep.wait_for_google]
}

# Grant GitHub Actions Service Account the ability to create and manage Cloud Build runs
resource "google_project_iam_member" "github_actions_cloudbuild_editor" {
  project = var.project_id
  role    = "roles/cloudbuild.builds.editor"
  member  = "serviceAccount:${google_service_account.github_actions_sa.email}"

  depends_on = [time_sleep.wait_for_google]
}

# Allow GitHub Actions Service Account to act as the Cloud Build Service Account
resource "google_service_account_iam_member" "github_actions_act_as_cloudbuild" {
  service_account_id = google_service_account.cloudbuild_sa.name
  role               = "roles/iam.serviceAccountUser"
  member             = "serviceAccount:${google_service_account.github_actions_sa.email}"

  depends_on = [time_sleep.wait_for_google]
}
