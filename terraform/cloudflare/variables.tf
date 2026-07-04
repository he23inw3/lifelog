# ─────────────────────────────────────────────────────────────
# Cloudflare Terraform — 変数定義
# ─────────────────────────────────────────────────────────────

# ──────────────────────────────────────
# Cloudflare 認証情報
# ──────────────────────────────────────

variable "cloudflare_api_token" {
  type        = string
  sensitive   = true
  description = <<-EOT
    以下の権限を持つ Cloudflare API トークン:
      - Account > Cloudflare Pages > Edit
      - Account > Workers Scripts > Edit
      - Zone > Zone Settings > Edit (カスタムドメイン使用時のみ)
      - Zone > DNS > Edit (カスタムドメイン使用時のみ)
    作成場所: https://dash.cloudflare.com/profile/api-tokens
  EOT
}

variable "cloudflare_account_id" {
  type        = string
  description = "Cloudflare アカウントID。ダッシュボードのゾーン概要ページまたは Workers 概要ページの右サイドバーで確認できる。"
}

# ──────────────────────────────────────
# アプリケーション設定
# ──────────────────────────────────────

variable "application_name" {
  type        = string
  default     = "lifelog"
  description = "Cloudflare リソース名のプレフィックスとして使用するアプリケーション名。"
}

# ──────────────────────────────────────
# GCP Cloud Run URL（GCP Terraform の output から取得）
# 取得方法: cd terraform/gcp && terraform output
# ──────────────────────────────────────

variable "webapi_url" {
  type        = string
  description = "Cloud Run webapi サービスの URL。取得方法: `cd terraform/gcp && terraform output -raw cloud_run_url`"
}

variable "demo_webapi_url" {
  type        = string
  description = "Cloud Run demo-webapi サービスの URL。取得方法: `cd terraform/gcp && terraform output -raw demo_cloud_run_url`"
}

# ──────────────────────────────────────
# Worker API URL（初回デプロイ後に設定）
# Worker をデプロイしたら *.workers.dev の URL をここに設定する。
# 設定するまでは Pages が Cloud Run URL に直接フォールバックする。
# ──────────────────────────────────────

variable "portal_api_url" {
  type        = string
  default     = ""
  description = "（Step 2）Portal API プロキシ Worker の URL。例: https://lifelog-webapi-proxy.myaccount.workers.dev"
}

variable "demo_api_url" {
  type        = string
  default     = ""
  description = "（Step 2）Demo API プロキシ Worker の URL。例: https://lifelog-demo-proxy.myaccount.workers.dev"
}

# ──────────────────────────────────────
# Pages オリジン（Worker の CORS 設定に使用）
# Pages プロジェクトをデプロイしたら設定する。
# ──────────────────────────────────────

variable "portal_pages_origin" {
  type        = string
  default     = ""
  description = "（Step 2）Worker の CORS ヘッダーに設定する Portal Pages の URL。例: https://lifelog-portal.pages.dev"
}

variable "demo_pages_origin" {
  type        = string
  default     = ""
  description = "（Step 2）Worker の CORS ヘッダーに設定する Demo Pages の URL。例: https://lifelog-demo.pages.dev"
}

# ──────────────────────────────────────
# カスタムドメイン設定
# ──────────────────────────────────────

variable "zone_id" {
  type        = string
  default     = ""
  description = "Cloudflare ゾーンID。カスタムドメインの DNS 管理に使用する。カスタムドメインを使用しない場合は空のままで可。"
}

variable "portal_custom_domain" {
  type        = string
  default     = ""
  description = "（任意）Portal のカスタムドメイン（例: portal.example.com）。zone_id が必要。"
}

variable "demo_custom_domain" {
  type        = string
  default     = ""
  description = "（任意）Demo のカスタムドメイン（例: demo.example.com）。zone_id が必要。"
}
