variable "application_name" {
  type        = string
  description = "The name of the application."
  default     = "lifelog"
}

variable "project_id" {
  type        = string
  description = "The Google Cloud Project ID where resources will be provisioned. Billing and APIs must be enabled."
}

variable "region" {
  type        = string
  description = "The Google Cloud region to deploy resources (e.g., asia-northeast1 for Tokyo)."
  default     = "asia-northeast1"
}

variable "environment" {
  type        = string
  description = "The application environment (e.g., development, production)."
  default     = "production"
}

variable "github_repo" {
  type        = string
  description = "The target GitHub repository for Workload Identity restrictions (e.g., 'username/repo')."
  default     = "he23inw3/lifelog"
}

variable "notification_email" {
  type        = string
  description = "The target email address for budget and system health alerts. If left empty, alerts are disabled."
  default     = ""
}

variable "container_image" {
  type        = string
  description = "The initial container image to use for the Cloud Run service and Jobs (e.g., hello image). Replaced by CI/CD later."
  default     = "us-docker.pkg.dev/cloudrun/container/hello"
}

variable "portal_cors_origin" {
  type        = string
  description = "The origin URL of the Portal frontend (Cloudflare Pages URL or custom domain). Used for Cloud Run CORS configuration."
  default     = "*"
}

variable "demo_cors_origin" {
  type        = string
  description = "The origin URL of the Demo frontend (Cloudflare Pages URL or custom domain). Used for Cloud Run CORS configuration."
  default     = "*"
}

variable "bootstrap_allowed_email" {
  type        = string
  description = "管理者が0人の場合に初回登録（ブートストラップ）を許可するメールアドレス"
  default     = ""
}
