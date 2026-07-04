output "cloud_run_url" {
  description = "The URL of the provisioned Cloud Run service for LifeLog API"
  value       = google_cloud_run_v2_service.services["webapi"].uri
}

output "artifact_registry_repository" {
  description = "The repository path in Artifact Registry"
  value       = "${google_artifact_registry_repository.lifelog_repo.location}-docker.pkg.dev/${var.project_id}/${google_artifact_registry_repository.lifelog_repo.repository_id}"
}

output "workload_identity_provider" {
  description = "The Workload Identity Provider resource name for GitHub Actions"
  value       = google_iam_workload_identity_pool_provider.github_provider.name
}

output "cloud_run_sa_email" {
  description = "The email of the Cloud Run execution Service Account"
  value       = google_service_account.cloud_run_sa.email
}

output "github_actions_sa_email" {
  description = "The email of the GitHub Actions deployment Service Account"
  value       = google_service_account.github_actions_sa.email
}

output "cloudbuild_sa_email" {
  description = "The email of the Cloud Build execution Service Account"
  value       = google_service_account.cloudbuild_sa.email
}

output "demo_cloud_run_url" {
  description = "The URL of the provisioned Cloud Run service for LifeLog Demo API"
  value       = google_cloud_run_v2_service.services["demo_webapi"].uri
}

