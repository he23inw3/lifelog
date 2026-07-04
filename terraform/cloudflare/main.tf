# ─────────────────────────────────────────────────────────────
# Cloudflare Terraform — プロバイダー & バックエンド
# ─────────────────────────────────────────────────────────────

terraform {
  required_version = ">= 1.6"

  required_providers {
    cloudflare = {
      source  = "cloudflare/cloudflare"
      version = "~> 4.0"
    }
  }
}

provider "cloudflare" {
  api_token = var.cloudflare_api_token
}

# ─────────────────────────────────────────────────────────────
# ローカル変数
# ─────────────────────────────────────────────────────────────

locals {
  # Portal Pages が VITE_API_BASE_URL として使用する API URL。
  # Worker のデプロイ前は Cloud Run の URL を直接参照する。
  # Worker デプロイ後は portal_api_url に Worker URL を設定すること。
  portal_api_url = var.portal_api_url != "" ? var.portal_api_url : var.webapi_url

  # Demo Pages が VITE_API_BASE_URL として使用する API URL。
  demo_api_url = var.demo_api_url != "" ? var.demo_api_url : var.demo_webapi_url

  # 各 Worker の CORS 許可オリジン。
  # 初回デプロイ時は "*" を設定し、Pages URL 確定後に絞り込む。
  portal_cors_origin = var.portal_pages_origin != "" ? var.portal_pages_origin : "*"
  demo_cors_origin   = var.demo_pages_origin != "" ? var.demo_pages_origin : "*"
}

# ─────────────────────────────────────────────────────────────
# ゾーン設定 — TLS
# ─────────────────────────────────────────────────────────────

resource "cloudflare_zone_settings_override" "tls" {
  count   = var.zone_id != "" ? 1 : 0
  zone_id = var.zone_id

  settings {
    # TLS 1.3 を最低バージョンとして強制する
    min_tls_version = "1.3"

    # TLS 1.3 + 0-RTT（ゼロラウンドトリップ再接続）を有効化
    # 0-RTT のリプレイ攻撃リスクが懸念される場合は "on" に変更すること
    tls_1_3 = "zrt"

    # Full (Strict) SSL: オリジン証明書の有効性を Cloudflare が検証する
    ssl = "strict"

    # HTTP Strict Transport Security (HSTS)
    security_header {
      enabled            = true
      include_subdomains = true
      max_age            = 31536000 # 1年
      nosniff            = true
      preload            = true
    }
  }
}
