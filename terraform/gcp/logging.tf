# Notification Channel for Alerts (Email)
resource "google_monitoring_notification_channel" "email_channel" {
  count        = var.notification_email != "" ? 1 : 0
  display_name = "Lifelog Alerts Email Channel"
  type         = "email"
  labels = {
    email_address = var.notification_email
  }

  depends_on = [google_project_service.services]
}

# Alert Policy for Cloud Run Service Error Logs
resource "google_monitoring_alert_policy" "cloud_run_error_alert" {
  count        = var.notification_email != "" ? 1 : 0
  display_name = "${var.application_name}-cloud-run-errors"
  combiner     = "OR"
  conditions {
    display_name = "Error log entries in Cloud Run"
    condition_matched_log {
      filter = "resource.type=\"cloud_run_revision\" AND severity>=ERROR"
    }
  }

  notification_channels = [
    google_monitoring_notification_channel.email_channel[0].id
  ]

  alert_strategy {
    notification_rate_limit {
      period = "3600s" # Throttles duplicate alerts to once per hour
    }
  }

  depends_on = [time_sleep.wait_for_google]
}
