# ─────────────────────────────────────────────────────────────
# Cloudflare Workers — Unit Tests (plan only)
# ─────────────────────────────────────────────────────────────

variables {
  cloudflare_api_token  = "mock-api-token-value-with-sufficient-length-12345"
  cloudflare_account_id = "mock-account-id"
  application_name      = "lifelog"
  webapi_url            = "https://test-webapi.run.app"
  demo_webapi_url       = "https://test-demo.run.app"
}

# ─────────────────────────────────────────────────────────────
# Workers Script
# ─────────────────────────────────────────────────────────────

# Portal用およびDemo用プロキシのWorkers Script名が正しい形式であること
run "workers_script_names" {
  command = plan

  variables {
    portal_pages_origin = ""
    demo_pages_origin   = ""
  }

  assert {
    condition     = cloudflare_workers_script.webapi_proxy.name == "lifelog-webapi-proxy"
    error_message = "webapi-proxy のスクリプト名は 'lifelog-webapi-proxy' であること"
  }

  assert {
    condition     = cloudflare_workers_script.demo_proxy.name == "lifelog-demo-proxy"
    error_message = "demo-proxy のスクリプト名は 'lifelog-demo-proxy' であること"
  }
}

# CORS許可オリジンのデフォルト値が '*' になっていること
run "workers_cors_default_fallback" {
  command = plan

  variables {
    portal_pages_origin = ""
    demo_pages_origin   = ""
  }

  assert {
    condition     = anytrue([for binding in cloudflare_workers_script.webapi_proxy.plain_text_binding : binding.text == "*" && binding.name == "ALLOWED_ORIGIN"])
    error_message = "未指定時の CORS 許可オリジンは '*' であること"
  }
}

# PagesのURLが指定された場合にCORS許可オリジンが正しく上書きされること
run "workers_cors_origin_injection" {
  command = plan

  variables {
    portal_pages_origin = "https://my-portal.pages.dev"
    demo_pages_origin   = "https://my-demo.pages.dev"
  }

  assert {
    condition     = anytrue([for binding in cloudflare_workers_script.webapi_proxy.plain_text_binding : binding.text == "https://my-portal.pages.dev" && binding.name == "ALLOWED_ORIGIN"])
    error_message = "portal_pages_origin が指定された場合、ALLOWED_ORIGIN は 'https://my-portal.pages.dev' であること"
  }

  assert {
    condition     = anytrue([for binding in cloudflare_workers_script.demo_proxy.plain_text_binding : binding.text == "https://my-demo.pages.dev" && binding.name == "ALLOWED_ORIGIN"])
    error_message = "demo_pages_origin が指定された場合、ALLOWED_ORIGIN は 'https://my-demo.pages.dev' であること"
  }
}

# ─────────────────────────────────────────────────────────────
# Workers Domain (Custom Domain)
# ─────────────────────────────────────────────────────────────

# カスタムドメインとゾーンが未指定の場合にWorkersドメインリソースが作成されないこと
run "workers_domain_disabled_by_default" {
  command = plan

  variables {
    portal_custom_domain = ""
    demo_custom_domain   = ""
    zone_id              = ""
  }

  assert {
    condition     = length(cloudflare_workers_domain.webapi_proxy) == 0
    error_message = "カスタムドメイン未指定時に Workers ドメインリソースが作成されないこと"
  }
}

# カスタムドメインとゾーンが指定された場合にWorkersドメインリソースが正しく作成されること
run "workers_domain_enabled" {
  command = plan

  variables {
    zone_id              = "mock-zone-id"
    portal_custom_domain = "example.com"
    demo_custom_domain   = "example.com"
  }

  assert {
    condition     = length(cloudflare_workers_domain.webapi_proxy) == 1
    error_message = "カスタムドメイン指定時に Workers ドメインリソース (webapi_proxy) が1つ作成されること"
  }

  assert {
    condition     = cloudflare_workers_domain.webapi_proxy[0].hostname == "api.example.com"
    error_message = "webapi-proxy のカスタムドメインホスト名は 'api.example.com' であること"
  }
}
