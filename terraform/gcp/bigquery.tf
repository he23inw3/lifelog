# BigQuery Dataset for LifeLog log storage
resource "google_bigquery_dataset" "lifelog_dataset" {
  dataset_id                  = "${var.application_name}_dataset"
  friendly_name               = "Lifelog Log Dataset"
  description                 = "Dataset containing daily log entries for Lifelog application"
  location                    = var.region
  default_table_expiration_ms = null # Keep logs indefinitely

  depends_on = [google_project_service.services]
}

# BigQuery Table daily_logs partitioned by log_date
resource "google_bigquery_table" "daily_logs" {
  dataset_id = google_bigquery_dataset.lifelog_dataset.dataset_id
  table_id   = "daily_logs"

  time_partitioning {
    type  = "DAY"
    field = "log_date"
  }

  require_partition_filter = true

  clustering = ["slack_user_id", "sentiment", "is_holiday"]

  schema = jsonencode([
    {
      name        = "slack_user_id"
      type        = "STRING"
      mode        = "REQUIRED"
      description = "Slack user ID to secure tenant isolation"
    },
    {
      name        = "log_date"
      type        = "DATE"
      mode        = "REQUIRED"
      description = "Target date for the daily log entry"
    },
    {
      name        = "raw_text"
      type        = "STRING"
      mode        = "REQUIRED"
      description = "Raw message from Slack"
    },
    {
      name        = "is_holiday"
      type        = "BOOLEAN"
      mode        = "REQUIRED"
      description = "Indicates if the target date is a holiday or a day-off"
    },
    {
      name        = "tasks"
      type        = "STRING"
      mode        = "NULLABLE"
      description = "Tasks extracted by AI"
    },
    {
      name        = "work_hours"
      type        = "FLOAT"
      mode        = "NULLABLE"
      description = "Hours spent on tasks"
    },
    {
      name        = "overtime_hours"
      type        = "FLOAT"
      mode        = "NULLABLE"
      description = "Overtime hours"
    },
    {
      name        = "diary"
      type        = "STRING"
      mode        = "NULLABLE"
      description = "Private diary section"
    },
    {
      name        = "sentiment"
      type        = "STRING"
      mode        = "NULLABLE"
      description = "Sentiment metric"
    },
    {
      name        = "trace_id"
      type        = "STRING"
      mode        = "NULLABLE"
      description = "Session trace identifier"
    },
    {
      name        = "created_at"
      type        = "TIMESTAMP"
      mode        = "REQUIRED"
      description = "Record creation timestamp"
    },
    {
      name        = "updated_at"
      type        = "TIMESTAMP"
      mode        = "REQUIRED"
      description = "Record last update timestamp"
    }
  ])

  # Disable deletion protection for flexibility during development/re-provisioning
  deletion_protection = false
}
