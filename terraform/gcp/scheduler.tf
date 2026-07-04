# Cloud Scheduler to trigger the Remind Batch Job hourly
resource "google_cloud_scheduler_job" "remind_scheduler" {
  name             = "${var.application_name}-remind-scheduler"
  description      = "Trigger lifelog-remind-batch job hourly"
  schedule         = "0 * * * *"
  time_zone        = "Asia/Tokyo"
  attempt_deadline = "660s" # Job timeout (600s) より長く設定する必要がある

  retry_config {
    retry_count = 1
  }

  http_target {
    http_method = "POST"
    uri         = "https://${var.region}-run.googleapis.com/apis/run.googleapis.com/v1/namespaces/${var.project_id}/jobs/${google_cloud_run_v2_job.jobs["remind"].name}:run"

    oauth_token {
      service_account_email = google_service_account.scheduler_sa.email
    }
  }

  depends_on = [
    time_sleep.wait_for_google
  ]
}

# Cloud Scheduler to trigger the Reflection Batch Job monthly at 23:00
resource "google_cloud_scheduler_job" "reflection_scheduler" {
  name             = "${var.application_name}-reflection-scheduler"
  description      = "Trigger lifelog-reflection-batch job monthly at 23:00"
  schedule         = "0 23 28-31 * *"
  time_zone        = "Asia/Tokyo"
  attempt_deadline = "960s" # Job timeout (900s) より長く設定する必要がある

  retry_config {
    retry_count = 1
  }

  http_target {
    http_method = "POST"
    uri         = "https://${var.region}-run.googleapis.com/apis/run.googleapis.com/v1/namespaces/${var.project_id}/jobs/${google_cloud_run_v2_job.jobs["reflection"].name}:run"

    oauth_token {
      service_account_email = google_service_account.scheduler_sa.email
    }
  }

  depends_on = [
    time_sleep.wait_for_google
  ]
}
