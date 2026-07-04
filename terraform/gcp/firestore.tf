# Firestore Native Database setup
resource "google_firestore_database" "database" {
  project                 = var.project_id
  name                    = "(default)"
  location_id             = var.region
  type                    = "FIRESTORE_NATIVE"
  delete_protection_state = "DELETE_PROTECTION_DISABLED"

  depends_on = [google_project_service.services]
}

# ─────────────────────────────────────────────────────────────
# Firestore Composite Indexes
# ─────────────────────────────────────────────────────────────

# system_jobs indexes
resource "google_firestore_index" "system_jobs_status_started_at" {
  project    = var.project_id
  database   = google_firestore_database.database.name
  collection = "system_jobs"

  fields {
    field_path = "status"
    order      = "ASCENDING"
  }
  fields {
    field_path = "startedAt"
    order      = "DESCENDING"
  }
}

resource "google_firestore_index" "system_jobs_job_type_started_at" {
  project    = var.project_id
  database   = google_firestore_database.database.name
  collection = "system_jobs"

  fields {
    field_path = "jobType"
    order      = "ASCENDING"
  }
  fields {
    field_path = "startedAt"
    order      = "DESCENDING"
  }
}

resource "google_firestore_index" "system_jobs_job_type_status_started_at" {
  project    = var.project_id
  database   = google_firestore_database.database.name
  collection = "system_jobs"

  fields {
    field_path = "jobType"
    order      = "ASCENDING"
  }
  fields {
    field_path = "status"
    order      = "ASCENDING"
  }
  fields {
    field_path = "startedAt"
    order      = "DESCENDING"
  }
}

# batch_execution_logs indexes
resource "google_firestore_index" "batch_execution_logs_status_started_at" {
  project    = var.project_id
  database   = google_firestore_database.database.name
  collection = "batch_execution_logs"

  fields {
    field_path = "status"
    order      = "ASCENDING"
  }
  fields {
    field_path = "startedAt"
    order      = "DESCENDING"
  }
}

resource "google_firestore_index" "batch_execution_logs_batch_name_started_at" {
  project    = var.project_id
  database   = google_firestore_database.database.name
  collection = "batch_execution_logs"

  fields {
    field_path = "batchName"
    order      = "ASCENDING"
  }
  fields {
    field_path = "startedAt"
    order      = "DESCENDING"
  }
}

resource "google_firestore_index" "batch_execution_logs_batch_name_status_started_at" {
  project    = var.project_id
  database   = google_firestore_database.database.name
  collection = "batch_execution_logs"

  fields {
    field_path = "batchName"
    order      = "ASCENDING"
  }
  fields {
    field_path = "status"
    order      = "ASCENDING"
  }
  fields {
    field_path = "startedAt"
    order      = "DESCENDING"
  }
}

# ─────────────────────────────────────────────────────────────
# Firestore Placeholders for Collections
# ─────────────────────────────────────────────────────────────

resource "google_firestore_document" "user_sessions_placeholder" {
  project     = var.project_id
  database    = google_firestore_database.database.name
  collection  = "user_sessions"
  document_id = "_placeholder"
  fields      = jsonencode({
    init = {
      stringValue = ""
    }
  })
}

resource "google_firestore_document" "user_settings_placeholder" {
  project     = var.project_id
  database    = google_firestore_database.database.name
  collection  = "user_settings"
  document_id = "_placeholder"
  fields      = jsonencode({
    init = {
      stringValue = ""
    }
  })
}

