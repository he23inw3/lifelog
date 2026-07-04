# ─────────────────────────────────────────────────────────────
# Cloud Scheduler — Unit Tests (plan only)
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
# Remind Scheduler
# ─────────────────────────────────────────────────────────────

# リマインドスケジューラーの名前がアプリ名を含む正しい形式であること
run "scheduler_remind_name" {
  command = plan

  assert {
    condition     = google_cloud_scheduler_job.remind_scheduler.name == "lifelog-remind-scheduler"
    error_message = "リマインドスケジューラーの名前は 'lifelog-remind-scheduler' であること"
  }
}

# リマインドスケジューラーが毎時実行のcron式を持つこと
run "scheduler_remind_cron" {
  command = plan

  assert {
    condition     = google_cloud_scheduler_job.remind_scheduler.schedule == "0 * * * *"
    error_message = "リマインドスケジューラーは毎時実行のcron式 '0 * * * *' を持つこと"
  }
}

# リマインドスケジューラーのタイムゾーンが東京であること
run "scheduler_remind_timezone" {
  command = plan

  assert {
    condition     = google_cloud_scheduler_job.remind_scheduler.time_zone == "Asia/Tokyo"
    error_message = "リマインドスケジューラーのタイムゾーンは 'Asia/Tokyo' であること"
  }
}

# リマインドスケジューラーのHTTPメソッドがPOSTであること
run "scheduler_remind_http_method" {
  command = plan

  assert {
    condition     = google_cloud_scheduler_job.remind_scheduler.http_target[0].http_method == "POST"
    error_message = "リマインドスケジューラーの HTTP メソッドは POST であること"
  }
}

# リマインドスケジューラーのリトライ回数が1回であること
run "scheduler_remind_retry_count" {
  command = plan

  assert {
    condition     = google_cloud_scheduler_job.remind_scheduler.retry_config[0].retry_count == 1
    error_message = "リマインドスケジューラーのリトライ回数は 1 であること"
  }
}

# リマインドスケジューラーのURIが正しいCloud_RunジョブのURLを指していること
run "scheduler_remind_uri" {
  command = plan

  assert {
    condition     = can(regex("/jobs/lifelog-remind-batch:run$", google_cloud_scheduler_job.remind_scheduler.http_target[0].uri))
    error_message = "リマインドスケジューラーの URI は 'lifelog-remind-batch:run' を対象とすること"
  }
}

# ─────────────────────────────────────────────────────────────
# Reflection Scheduler
# ─────────────────────────────────────────────────────────────

# リフレクションスケジューラーの名前がアプリ名を含む正しい形式であること
run "scheduler_reflection_name" {
  command = plan

  assert {
    condition     = google_cloud_scheduler_job.reflection_scheduler.name == "lifelog-reflection-scheduler"
    error_message = "リフレクションスケジューラーの名前は 'lifelog-reflection-scheduler' であること"
  }
}

# リフレクションスケジューラーが月末23時実行のcron式を持つこと
run "scheduler_reflection_cron" {
  command = plan

  assert {
    condition     = google_cloud_scheduler_job.reflection_scheduler.schedule == "0 23 28-31 * *"
    error_message = "リフレクションスケジューラーは月末23時実行のcron式 '0 23 28-31 * *' を持つこと"
  }
}

# リフレクションスケジューラーのタイムゾーンが東京であること
run "scheduler_reflection_timezone" {
  command = plan

  assert {
    condition     = google_cloud_scheduler_job.reflection_scheduler.time_zone == "Asia/Tokyo"
    error_message = "リフレクションスケジューラーのタイムゾーンは 'Asia/Tokyo' であること"
  }
}

# リフレクションスケジューラーのリトライ回数が1回であること
run "scheduler_reflection_retry_count" {
  command = plan

  assert {
    condition     = google_cloud_scheduler_job.reflection_scheduler.retry_config[0].retry_count == 1
    error_message = "リフレクションスケジューラーのリトライ回数は 1 であること"
  }
}

# リフレクションスケジューラーのURIが正しいCloud_RunジョブのURLを指していること
run "scheduler_reflection_uri" {
  command = plan

  assert {
    condition     = can(regex("/jobs/lifelog-reflection-batch:run$", google_cloud_scheduler_job.reflection_scheduler.http_target[0].uri))
    error_message = "リフレクションスケジューラーの URI は 'lifelog-reflection-batch:run' を対象とすること"
  }
}
