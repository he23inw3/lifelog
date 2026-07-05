# Locals for Cloud Run Services & Jobs to avoid boilerplate duplication
locals {
  # Common environment variables for all Cloud Run containers
  common_envs = [
    { name = "GOOGLE_CLOUD_PROJECT", value = var.project_id },
    { name = "BIGQUERY_DATASET", value = google_bigquery_dataset.lifelog_dataset.dataset_id },
    { name = "GOOGLE_GENAI_USE_VERTEXAI", value = "true" },
    { name = "GEMINI_LOCATION", value = "us-central1" },
    { name = "GEMINI_MODEL", value = "gemini-2.5-flash-lite" }
  ]

  # All available secrets in Secret Manager
  secrets = {
    slack_signing_secret = {
      name      = "SLACK_SIGNING_SECRET"
      secret_id = google_secret_manager_secret.slack_signing_secret.secret_id
    }
    slack_bot_token = {
      name      = "SLACK_BOT_TOKEN"
      secret_id = google_secret_manager_secret.slack_bot_token.secret_id
    }
    oidc_client_id = {
      name      = "OIDC_CLIENT_ID"
      secret_id = google_secret_manager_secret.oidc_client_id.secret_id
    }
    crypto_key = {
      name      = "CRYPTO_KEY"
      secret_id = google_secret_manager_secret.crypto_key.secret_id
    }
  }

  # Config definitions for Services (only keys that differ)
  cloud_run_services = {
    webapi = {
      suffix       = "webapi"
      demo_mode    = "false"
      cors_origins = var.portal_cors_origin
    }
    demo_webapi = {
      suffix       = "demo-webapi"
      demo_mode    = "true"
      cors_origins = var.demo_cors_origin
    }
  }

  # Config definitions for Jobs (only keys that differ)
  cloud_run_jobs = {
    remind = {
      suffix  = "remind-batch"
      profile = "remind-batch"
      timeout = "600s"
    }
    reflection = {
      suffix  = "reflection-batch"
      profile = "reflection-batch"
      timeout = "900s"
    }
  }
}

# Cloud Run Services
resource "google_cloud_run_v2_service" "services" {
  for_each = local.cloud_run_services

  name                = "${var.application_name}-${each.value.suffix}"
  location            = var.region
  ingress             = "INGRESS_TRAFFIC_ALL"
  deletion_protection = false

  template {
    execution_environment = "EXECUTION_ENVIRONMENT_GEN2"
    timeout               = "30s"
    service_account       = google_service_account.cloud_run_sa.email

    containers {
      image = var.container_image

      # Merge common and service-specific env vars
      dynamic "env" {
        for_each = concat(
          local.common_envs,
          [
            { name = "APP_ENV", value = var.environment },
            { name = "DEMO_MODE", value = each.value.demo_mode },
            { name = "CORS_ORIGINS", value = each.value.cors_origins },
            { name = "BOOTSTRAP_ALLOWED_EMAIL", value = var.bootstrap_allowed_email }
          ]
        )
        content {
          name  = env.value.name
          value = env.value.value
        }
      }

      # Dynamically reference service secrets directly
      dynamic "env" {
        for_each = [
          local.secrets.slack_signing_secret,
          local.secrets.slack_bot_token,
          local.secrets.oidc_client_id,
          local.secrets.crypto_key
        ]
        content {
          name = env.value.name
          value_source {
            secret_key_ref {
              secret  = env.value.secret_id
              version = "latest"
            }
          }
        }
      }

      resources {
        limits = {
          cpu    = "1"
          memory = "512Mi"
        }
        cpu_idle          = false
        startup_cpu_boost = true
      }
    }

    scaling {
      min_instance_count = 0
      max_instance_count = 1
    }
  }

  lifecycle {
    ignore_changes = [
      template[0].containers[0].image,
    ]
  }

  depends_on = [
    time_sleep.wait_for_google
  ]
}

# Cloud Run Jobs
resource "google_cloud_run_v2_job" "jobs" {
  for_each = local.cloud_run_jobs

  name                = "${var.application_name}-${each.value.suffix}"
  location            = var.region
  deletion_protection = false

  template {
    template {
      service_account = google_service_account.cloud_run_sa.email
      timeout         = each.value.timeout

      containers {
        image = var.container_image

        # Merge common and job-specific env vars
        dynamic "env" {
          for_each = concat(
            local.common_envs,
            [
              { name = "QUARKUS_PROFILE", value = each.value.profile }
            ]
          )
          content {
            name  = env.value.name
            value = env.value.value
          }
        }

        # Dynamically reference job secrets directly
        dynamic "env" {
          for_each = [
            local.secrets.slack_bot_token,
            local.secrets.crypto_key
          ]
          content {
            name = env.value.name
            value_source {
              secret_key_ref {
                secret  = env.value.secret_id
                version = "latest"
              }
            }
          }
        }

        resources {
          limits = {
            cpu    = "1"
            memory = "512Mi"
          }
        }
      }
    }
  }

  lifecycle {
    ignore_changes = [
      template[0].template[0].containers[0].image,
    ]
  }

  depends_on = [
    time_sleep.wait_for_google
  ]
}

# State Migrations (Moved Blocks)
moved {
  from = google_cloud_run_v2_service.lifelog_webapi
  to   = google_cloud_run_v2_service.services["webapi"]
}

moved {
  from = google_cloud_run_v2_service.lifelog_demo_webapi
  to   = google_cloud_run_v2_service.services["demo_webapi"]
}

moved {
  from = google_cloud_run_v2_job.lifelog_remind_batch
  to   = google_cloud_run_v2_job.jobs["remind"]
}

moved {
  from = google_cloud_run_v2_job.lifelog_reflection_batch
  to   = google_cloud_run_v2_job.jobs["reflection"]
}

# Allow unauthenticated (public) access to Cloud Run services (webapi and demo_webapi)
resource "google_cloud_run_v2_service_iam_member" "public_access" {
  for_each = local.cloud_run_services

  name     = google_cloud_run_v2_service.services[each.key].name
  location = google_cloud_run_v2_service.services[each.key].location
  role     = "roles/run.invoker"
  member   = "allUsers"
}

