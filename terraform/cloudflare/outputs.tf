# ─────────────────────────────────────────────────────────────
# Cloudflare Terraform — 出力値
# ─────────────────────────────────────────────────────────────

output "portal_pages_url" {
  description = "Portal アプリの Cloudflare Pages URL"
  value       = "https://${cloudflare_pages_project.portal.name}.pages.dev"
}

output "demo_pages_url" {
  description = "Demo アプリの Cloudflare Pages URL"
  value       = "https://${cloudflare_pages_project.demo.name}.pages.dev"
}

output "webapi_proxy_worker_name" {
  description = "Portal API プロキシ Worker の名前。workers.dev URL: https://{この値}.{アカウントサブドメイン}.workers.dev"
  value       = cloudflare_workers_script.webapi_proxy.name
}

output "demo_proxy_worker_name" {
  description = "Demo API プロキシ Worker の名前。workers.dev URL: https://{この値}.{アカウントサブドメイン}.workers.dev"
  value       = cloudflare_workers_script.demo_proxy.name
}

output "next_steps" {
  description = "セットアップ完了のための手順"
  value       = <<-EOT
    ────────────────────────────────────────────────────────────
    次のステップ（初回 `terraform apply` 後に実施）
    ────────────────────────────────────────────────────────────

    1. workers.dev ルートを有効化する:
       → Cloudflare ダッシュボード > Workers & Pages
       → 各 Worker を選択 > Settings > Triggers > workers.dev: Enable

    2. Worker の URL を控える（形式: https://{worker名}.{アカウントサブドメイン}.workers.dev）
       → webapi-proxy URL: portal_api_url に使用
       → demo-proxy URL:   demo_api_url に使用

    3. Pages の URL を控える:
       → Portal: ${cloudflare_pages_project.portal.name}.pages.dev
       → Demo:   ${cloudflare_pages_project.demo.name}.pages.dev

    4. 以下の変数を設定して terraform apply を再実行する:
         portal_api_url      = "<webapi-proxy の workers.dev URL>"
         demo_api_url        = "<demo-proxy の workers.dev URL>"
         portal_pages_origin = "https://${cloudflare_pages_project.portal.name}.pages.dev"
         demo_pages_origin   = "https://${cloudflare_pages_project.demo.name}.pages.dev"

    5. GCP Terraform の CORS 変数を更新する:
         portal_cors_origin = "https://${cloudflare_pages_project.portal.name}.pages.dev"
         demo_cors_origin   = "https://${cloudflare_pages_project.demo.name}.pages.dev"
       その後: cd ../gcp && terraform apply

    6. Cloudflare ダッシュボードから GitHub リポジトリを Pages に接続する:
       → Workers & Pages > ${cloudflare_pages_project.portal.name} > Settings > Build & deployments
    ────────────────────────────────────────────────────────────
  EOT
}
