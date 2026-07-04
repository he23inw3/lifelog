# ─────────────────────────────────────────────────────────────
# Cloudflare Workers — API リバースプロキシ
# ─────────────────────────────────────────────────────────────
#
# 以下の2つの Worker を作成する:
#   1. webapi-proxy  → Portal フロントエンド → Cloud Run webapi へプロキシ
#   2. demo-proxy    → Demo フロントエンド   → Cloud Run demo-webapi へプロキシ
#
# apply 後の手順:
#   1. Cloudflare ダッシュボードで workers.dev ルートを有効化する:
#      Workers & Pages → {worker} → Settings → Triggers → workers.dev → Enable
#   2. portal_api_url / demo_api_url に workers.dev の URL を設定する
#   3. terraform apply を再実行して Pages の環境変数を更新する
# ─────────────────────────────────────────────────────────────

# ──────────────────────────────────────
# Portal — webapi プロキシ Worker
# ──────────────────────────────────────

resource "cloudflare_workers_script" "webapi_proxy" {
  account_id = var.cloudflare_account_id
  name       = "${var.application_name}-webapi-proxy"
  content    = file("${path.module}/workers/api_proxy.js")
  module     = true

  # Cloud Run webapi の URL（Worker UI への露出を防ぐためシークレットとして保存）
  secret_text_binding {
    name = "UPSTREAM_URL"
    text = var.webapi_url
  }

  # CORS 許可オリジン — Portal Pages の URL
  # 初回デプロイ時は "*" を設定し、Step 2 で実際の Pages URL に絞り込む
  plain_text_binding {
    name = "ALLOWED_ORIGIN"
    text = local.portal_cors_origin
  }
}

# webapi プロキシのカスタムドメイン（portal_custom_domain と zone_id が設定されている場合のみ作成）
resource "cloudflare_workers_domain" "webapi_proxy" {
  count      = var.portal_custom_domain != "" && var.zone_id != "" ? 1 : 0
  account_id = var.cloudflare_account_id
  hostname   = "api.${var.portal_custom_domain}"
  service    = cloudflare_workers_script.webapi_proxy.name
  zone_id    = var.zone_id
}

# ──────────────────────────────────────
# Demo — demo-webapi プロキシ Worker
# ──────────────────────────────────────

resource "cloudflare_workers_script" "demo_proxy" {
  account_id = var.cloudflare_account_id
  name       = "${var.application_name}-demo-proxy"
  content    = file("${path.module}/workers/api_proxy.js")
  module     = true

  # Cloud Run demo-webapi の URL（シークレットとして保存）
  secret_text_binding {
    name = "UPSTREAM_URL"
    text = var.demo_webapi_url
  }

  # CORS 許可オリジン — Demo Pages の URL
  plain_text_binding {
    name = "ALLOWED_ORIGIN"
    text = local.demo_cors_origin
  }
}

# demo プロキシのカスタムドメイン（demo_custom_domain と zone_id が設定されている場合のみ作成）
resource "cloudflare_workers_domain" "demo_proxy" {
  count      = var.demo_custom_domain != "" && var.zone_id != "" ? 1 : 0
  account_id = var.cloudflare_account_id
  hostname   = "api.${var.demo_custom_domain}"
  service    = cloudflare_workers_script.demo_proxy.name
  zone_id    = var.zone_id
}
