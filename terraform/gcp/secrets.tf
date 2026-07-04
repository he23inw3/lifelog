# Secret Manager Secrets for storing credentials and configs securely
resource "google_secret_manager_secret" "slack_signing_secret" {
  secret_id = "${var.application_name}-slack-signing-secret"
  replication {
    auto {}
  }
  depends_on = [google_project_service.services]
}

resource "google_secret_manager_secret_version" "slack_signing_secret_version" {
  secret      = google_secret_manager_secret.slack_signing_secret.id
  secret_data = "dummy-slack-signing-secret"
}

resource "google_secret_manager_secret" "slack_bot_token" {
  secret_id = "${var.application_name}-slack-bot-token"
  replication {
    auto {}
  }
  depends_on = [google_project_service.services]
}

resource "google_secret_manager_secret_version" "slack_bot_token_version" {
  secret      = google_secret_manager_secret.slack_bot_token.id
  secret_data = "dummy-slack-bot-token"
}

resource "google_secret_manager_secret" "oidc_client_id" {
  secret_id = "${var.application_name}-oidc-client-id"
  replication {
    auto {}
  }
  depends_on = [google_project_service.services]
}

resource "google_secret_manager_secret_version" "oidc_client_id_version" {
  secret      = google_secret_manager_secret.oidc_client_id.id
  secret_data = "dummy-oidc-client-id"
}

resource "google_secret_manager_secret" "crypto_key" {
  secret_id = "${var.application_name}-crypto-key"
  replication {
    auto {}
  }
  depends_on = [google_project_service.services]
}

resource "google_secret_manager_secret_version" "crypto_key_version" {
  secret      = google_secret_manager_secret.crypto_key.id
  secret_data = "dummy-crypto-key-replace-me-with-32-byte-hex-string"
}
