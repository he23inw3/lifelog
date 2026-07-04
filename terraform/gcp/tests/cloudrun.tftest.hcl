# ─────────────────────────────────────────────────────────────
# Cloud Run Services & Jobs — Unit Tests (plan only)
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
# Cloud Run Services
# ─────────────────────────────────────────────────────────────

# Cloud_Runサービス名がアプリ名とサフィックスの組み合わせで正しいこと
run "cloud_run_service_names" {
  command = plan

  assert {
    condition     = google_cloud_run_v2_service.services["webapi"].name == "lifelog-webapi"
    error_message = "webapi のサービス名は 'lifelog-webapi' であること"
  }

  assert {
    condition     = google_cloud_run_v2_service.services["demo_webapi"].name == "lifelog-demo-webapi"
    error_message = "demo-webapi のサービス名は 'lifelog-demo-webapi' であること"
  }
}

# Cloud_Runサービスのデプロイリージョンが正しいこと
run "cloud_run_service_regions" {
  command = plan

  assert {
    condition     = google_cloud_run_v2_service.services["webapi"].location == "asia-northeast1"
    error_message = "webapi のデプロイリージョンは 'asia-northeast1' であること"
  }

  assert {
    condition     = google_cloud_run_v2_service.services["demo_webapi"].location == "asia-northeast1"
    error_message = "demo-webapi のデプロイリージョンは 'asia-northeast1' であること"
  }
}

# Cloud_Runサービスの受信トラフィックが全許可であること
run "cloud_run_service_ingress_all" {
  command = plan

  assert {
    condition     = google_cloud_run_v2_service.services["webapi"].ingress == "INGRESS_TRAFFIC_ALL"
    error_message = "webapi の ingress は INGRESS_TRAFFIC_ALL であること"
  }
}

# Cloud_Runサービスのスケーリング設定がゼロスケール可能かつ上限1インスタンスであること
run "cloud_run_service_scaling" {
  command = plan

  assert {
    condition     = google_cloud_run_v2_service.services["webapi"].template[0].scaling[0].min_instance_count == 0
    error_message = "webapi の最小インスタンス数は 0（ゼロスケール可能）であること"
  }

  assert {
    condition     = google_cloud_run_v2_service.services["webapi"].template[0].scaling[0].max_instance_count == 1
    error_message = "webapi の最大インスタンス数はコスト制御のため 1 であること"
  }
}

# Cloud_RunサービスのデモモードフラグがService種別ごとに正しいこと
run "cloud_run_service_demo_mode_flags" {
  command = plan

  # demo-webapi は DEMO_MODE=true、webapi は DEMO_MODE=false であること
  assert {
    condition = contains(
      [for e in google_cloud_run_v2_service.services["demo_webapi"].template[0].containers[0].env : e.value if e.name == "DEMO_MODE"],
      "true"
    )
    error_message = "demo-webapi の DEMO_MODE 環境変数は 'true' であること"
  }

  assert {
    condition = contains(
      [for e in google_cloud_run_v2_service.services["webapi"].template[0].containers[0].env : e.value if e.name == "DEMO_MODE"],
      "false"
    )
    error_message = "webapi の DEMO_MODE 環境変数は 'false' であること"
  }
}

# Cloud_RunサービスのCPU・メモリ上限が正しいこと
run "cloud_run_service_resources" {
  command = plan

  assert {
    condition     = google_cloud_run_v2_service.services["webapi"].template[0].containers[0].resources[0].limits["cpu"] == "1"
    error_message = "webapi の CPU 上限は '1' であること"
  }

  assert {
    condition     = google_cloud_run_v2_service.services["webapi"].template[0].containers[0].resources[0].limits["memory"] == "512Mi"
    error_message = "webapi のメモリ上限は '512Mi' であること"
  }
}

# Cloud_RunサービスがGen2実行環境を使用していること
run "cloud_run_service_gen2" {
  command = plan

  assert {
    condition     = google_cloud_run_v2_service.services["webapi"].template[0].execution_environment == "EXECUTION_ENVIRONMENT_GEN2"
    error_message = "webapi の実行環境は EXECUTION_ENVIRONMENT_GEN2 であること"
  }
}

# ─────────────────────────────────────────────────────────────
# Cloud Run Jobs
# ─────────────────────────────────────────────────────────────

# Cloud_RunジョブのJob名がアプリ名とサフィックスの組み合わせで正しいこと
run "cloud_run_job_names" {
  command = plan

  assert {
    condition     = google_cloud_run_v2_job.jobs["remind"].name == "lifelog-remind-batch"
    error_message = "remind ジョブ名は 'lifelog-remind-batch' であること"
  }

  assert {
    condition     = google_cloud_run_v2_job.jobs["reflection"].name == "lifelog-reflection-batch"
    error_message = "reflection ジョブ名は 'lifelog-reflection-batch' であること"
  }
}

# Cloud_Runジョブのタイムアウト設定がジョブ種別ごとに正しいこと
run "cloud_run_job_timeouts" {
  command = plan

  assert {
    condition     = google_cloud_run_v2_job.jobs["remind"].template[0].template[0].timeout == "600s"
    error_message = "remind-batch のタイムアウトは 600s であること"
  }

  assert {
    condition     = google_cloud_run_v2_job.jobs["reflection"].template[0].template[0].timeout == "900s"
    error_message = "reflection-batch のタイムアウトは 900s であること"
  }
}

# Cloud_RunジョブのQUARKUS_PROFILE環境変数がジョブ種別ごとに正しいこと
run "cloud_run_job_quarkus_profiles" {
  command = plan

  assert {
    condition = contains(
      [for e in google_cloud_run_v2_job.jobs["remind"].template[0].template[0].containers[0].env : e.value if e.name == "QUARKUS_PROFILE"],
      "remind-batch"
    )
    error_message = "remind ジョブの QUARKUS_PROFILE は 'remind-batch' であること"
  }

  assert {
    condition = contains(
      [for e in google_cloud_run_v2_job.jobs["reflection"].template[0].template[0].containers[0].env : e.value if e.name == "QUARKUS_PROFILE"],
      "reflection-batch"
    )
    error_message = "reflection ジョブの QUARKUS_PROFILE は 'reflection-batch' であること"
  }
}
