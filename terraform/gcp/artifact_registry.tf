# Artifact Registry Repository for LifeLog Docker images
resource "google_artifact_registry_repository" "lifelog_repo" {
  location      = var.region
  repository_id = "${var.application_name}-repo"
  description   = "Docker repository for ${var.application_name}"
  format        = "DOCKER"

  # 1. Delete untagged images (remnants of failed builds/temporary versions)
  cleanup_policies {
    id     = "delete-untagged"
    action = "DELETE"
    condition {
      tag_state = "UNTAGGED"
    }
  }

  # 2. Keep the latest 2 versions of the lifelog app image
  cleanup_policies {
    id     = "keep-latest-versions"
    action = "KEEP"
    most_recent_versions {
      package_name_prefixes = ["lifelog"]
      keep_count            = 2
    }
  }

  # 3. Delete all older versions not matched by KEEP policies
  cleanup_policies {
    id     = "delete-old-versions"
    action = "DELETE"
    condition {
      tag_state = "ANY"
    }
  }

  depends_on = [google_project_service.services]
}
