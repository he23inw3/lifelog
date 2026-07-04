# ─────────────────────────────────────────────────────────────
# Cloudflare Pages — Unit Tests (plan only)
# ─────────────────────────────────────────────────────────────

variables {
  cloudflare_api_token  = "mock-api-token-value-with-sufficient-length-12345"
  cloudflare_account_id = "mock-account-id"
  application_name      = "lifelog"
  webapi_url            = "https://test-webapi.run.app"
  demo_webapi_url       = "https://test-demo.run.app"
}

# ─────────────────────────────────────────────────────────────
# Pages Project
# ─────────────────────────────────────────────────────────────

# Pages プロジェクト名が正しい形式であること
run "pages_project_names" {
  command = plan

  assert {
    condition     = cloudflare_pages_project.portal.name == "lifelog-portal"
    error_message = "portal アプリの Pages プロジェクト名は 'lifelog-portal' であること"
  }

  assert {
    condition     = cloudflare_pages_project.demo.name == "lifelog-demo"
    error_message = "demo アプリの Pages プロジェクト名は 'lifelog-demo' であること"
  }
}

# ─────────────────────────────────────────────────────────────
# Pages Domain (Custom Domain)
# ─────────────────────────────────────────────────────────────

# カスタムドメインとゾーンが未指定の場合にPagesドメインリソースが作成されないこと
run "pages_domain_disabled_by_default" {
  command = plan

  variables {
    portal_custom_domain = ""
    demo_custom_domain   = ""
    zone_id              = ""
  }

  assert {
    condition     = length(cloudflare_pages_domain.portal_custom) == 0
    error_message = "カスタムドメイン未指定時に Pages ドメインリソースが作成されないこと"
  }
}

# カスタムドメインとゾーンが指定された場合にPagesドメインリソースが正しく作成されること
run "pages_domain_enabled" {
  command = plan

  variables {
    zone_id              = "mock-zone-id"
    portal_custom_domain = "portal.example.com"
    demo_custom_domain   = "demo.example.com"
  }

  assert {
    condition     = length(cloudflare_pages_domain.portal_custom) == 1
    error_message = "カスタムドメイン指定時に Pages ドメインリソース (portal_custom) が1つ作成されること"
  }

  assert {
    condition     = cloudflare_pages_domain.portal_custom[0].domain == "portal.example.com"
    error_message = "portal_custom のカスタムドメイン名は 'portal.example.com' であること"
  }
}
