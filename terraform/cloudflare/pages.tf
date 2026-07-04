# ─────────────────────────────────────────────────────────────
# Cloudflare Pages — Portal & Demo
# ─────────────────────────────────────────────────────────────
#
# Cloudflare Pages プロジェクトを作成するリソース。
# GitHub リポジトリの連携はCloudflare ダッシュボードから手動で行う:
#   https://dash.cloudflare.com → Pages → Connect to Git
# ─────────────────────────────────────────────────────────────

# ──────────────────────────────────────
# Portal アプリ
# ──────────────────────────────────────

resource "cloudflare_pages_project" "portal" {
  account_id        = var.cloudflare_account_id
  name              = "${var.application_name}-portal"
  production_branch = "main"

  # build_config と deployment_configs は cloudflare provider v4.x では非サポート。
  # ビルド設定と環境変数は Cloudflare ダッシュボードから設定する:
  #   → Pages → <プロジェクト名> → Settings → Environment variables
  #   VITE_API_BASE_URL = <Cloud Run または Worker の URL>
  #   NODE_VERSION      = 20
}

# Portal のカスタムドメイン（任意 — portal_custom_domain と zone_id が設定されている場合のみ作成）
resource "cloudflare_pages_domain" "portal_custom" {
  count        = var.portal_custom_domain != "" && var.zone_id != "" ? 1 : 0
  account_id   = var.cloudflare_account_id
  project_name = cloudflare_pages_project.portal.name
  domain       = var.portal_custom_domain
}

# ──────────────────────────────────────
# Demo アプリ
# ──────────────────────────────────────

resource "cloudflare_pages_project" "demo" {
  account_id        = var.cloudflare_account_id
  name              = "${var.application_name}-demo"
  production_branch = "main"

  # build_config と deployment_configs は cloudflare provider v4.x では非サポート。
  # ビルド設定と環境変数は Cloudflare ダッシュボードから設定する:
  #   → Pages → <プロジェクト名> → Settings → Environment variables
  #   VITE_API_BASE_URL = <Cloud Run または Worker の URL>
  #   NODE_VERSION      = 20
}

# Demo のカスタムドメイン（任意 — demo_custom_domain と zone_id が設定されている場合のみ作成）
resource "cloudflare_pages_domain" "demo_custom" {
  count        = var.demo_custom_domain != "" && var.zone_id != "" ? 1 : 0
  account_id   = var.cloudflare_account_id
  project_name = cloudflare_pages_project.demo.name
  domain       = var.demo_custom_domain
}
