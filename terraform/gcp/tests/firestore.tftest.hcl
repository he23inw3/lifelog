# ─────────────────────────────────────────────────────────────
# Firestore Database, Indexes & Placeholders — Unit Tests (plan only)
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
# Database
# ─────────────────────────────────────────────────────────────

# FirestoreデータベースタイプがNATIVEモードであること
run "firestore_database_type_native" {
  command = plan

  assert {
    condition     = google_firestore_database.database.type == "FIRESTORE_NATIVE"
    error_message = "Firestore データベースタイプは FIRESTORE_NATIVE であること"
  }
}

# Firestoreデータベースのロケーションが正しいこと
run "firestore_database_location" {
  command = plan

  assert {
    condition     = google_firestore_database.database.location_id == "asia-northeast1"
    error_message = "Firestore データベースのロケーションは 'asia-northeast1' であること"
  }
}

# Firestoreデータベース名がdefaultであること
run "firestore_database_name_default" {
  command = plan

  assert {
    condition     = google_firestore_database.database.name == "(default)"
    error_message = "Firestore データベース名は '(default)' であること"
  }
}

# Firestoreデータベースの削除保護が無効であること
run "firestore_database_deletion_protection_disabled" {
  command = plan

  assert {
    condition     = google_firestore_database.database.delete_protection_state == "DELETE_PROTECTION_DISABLED"
    error_message = "Firestore の削除保護は DELETE_PROTECTION_DISABLED であること"
  }
}

# ─────────────────────────────────────────────────────────────
# Placeholder Documents
# ─────────────────────────────────────────────────────────────

# user_sessionsコレクションにプレースホルダードキュメントが作成されること
run "firestore_user_sessions_placeholder" {
  command = plan

  assert {
    condition     = google_firestore_document.user_sessions_placeholder.collection == "user_sessions"
    error_message = "user_sessions プレースホルダーは 'user_sessions' コレクションに作成されること"
  }

  assert {
    condition     = google_firestore_document.user_sessions_placeholder.document_id == "_placeholder"
    error_message = "user_sessions プレースホルダーのドキュメント ID は '_placeholder' であること"
  }
}

# user_settingsコレクションにプレースホルダードキュメントが作成されること
run "firestore_user_settings_placeholder" {
  command = plan

  assert {
    condition     = google_firestore_document.user_settings_placeholder.collection == "user_settings"
    error_message = "user_settings プレースホルダーは 'user_settings' コレクションに作成されること"
  }

  assert {
    condition     = google_firestore_document.user_settings_placeholder.document_id == "_placeholder"
    error_message = "user_settings プレースホルダーのドキュメント ID は '_placeholder' であること"
  }
}

# ─────────────────────────────────────────────────────────────
# Composite Indexes — system_jobs
# ─────────────────────────────────────────────────────────────

# system_jobsのstatusとstartedAtの複合インデックスが正しいコレクションを対象としていること
run "firestore_index_system_jobs_status_started_at" {
  command = plan

  assert {
    condition     = google_firestore_index.system_jobs_status_started_at.collection == "system_jobs"
    error_message = "system_jobs_status_started_at インデックスは 'system_jobs' コレクションを対象とすること"
  }
}

# system_jobsのjobTypeとstatusとstartedAtの複合インデックスが3フィールドを持つこと
run "firestore_index_system_jobs_job_type_status_started_at" {
  command = plan

  assert {
    condition     = length(google_firestore_index.system_jobs_job_type_status_started_at.fields) == 3
    error_message = "system_jobs の jobType+status+startedAt 複合インデックスはフィールド数が 3 であること"
  }
}

# ─────────────────────────────────────────────────────────────
# Composite Indexes — batch_execution_logs
# ─────────────────────────────────────────────────────────────

# batch_execution_logsのstatusとstartedAtの複合インデックスが正しいコレクションを対象としていること
run "firestore_index_batch_execution_logs_status_started_at" {
  command = plan

  assert {
    condition     = google_firestore_index.batch_execution_logs_status_started_at.collection == "batch_execution_logs"
    error_message = "batch_execution_logs_status_started_at インデックスは 'batch_execution_logs' コレクションを対象とすること"
  }
}

# batch_execution_logsのbatchNameとstatusとstartedAtの複合インデックスが3フィールドを持つこと
run "firestore_index_batch_execution_logs_batch_name_status_started_at" {
  command = plan

  assert {
    condition     = length(google_firestore_index.batch_execution_logs_batch_name_status_started_at.fields) == 3
    error_message = "batch_execution_logs の batchName+status+startedAt 複合インデックスはフィールド数が 3 であること"
  }
}
