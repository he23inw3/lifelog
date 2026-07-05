/**
 * Cloudflare Worker — Cloud Run Reverse Proxy
 *
 * Forwards requests from a Cloudflare Pages frontend to the Cloud Run
 * backend service, injecting CORS headers so the browser can call the API.
 */

/** @type {string[]} */
const CORS_ALLOW_METHODS = "GET, POST, PUT, DELETE, PATCH, OPTIONS";

/** @type {string[]} */
const CORS_ALLOW_HEADERS = "Authorization, Content-Type, Accept, X-Requested-With";

/**
 * Dynamic CORS origin helper that supports exact match, wildcard fallback for credentials,
 * and Cloudflare Pages preview URLs (subdomains of the configured allowed origin).
 * 
 * @param {Request} request
 * @param {string} allowedOrigin
 * @returns {string}
 */
function getCorsOrigin(request, allowedOrigin) {
  const requestOrigin = request.headers.get("origin");
  if (!requestOrigin) {
    return allowedOrigin;
  }
  
  if (allowedOrigin === "*") {
    return requestOrigin;
  }
  
  if (requestOrigin === allowedOrigin) {
    return requestOrigin;
  }
  
  // Support Cloudflare Pages preview subdomains (e.g. https://[hash].[project].pages.dev)
  try {
    const allowedUrl = new URL(allowedOrigin);
    const allowedHost = allowedUrl.hostname; // e.g. lifelog-demo.pages.dev
    const requestUrl = new URL(requestOrigin);
    const requestHost = requestUrl.hostname; // e.g. db662f7b.lifelog-demo.pages.dev
    
    if (requestHost === allowedHost || requestHost.endsWith("." + allowedHost)) {
      return requestOrigin;
    }
  } catch (e) {
    // Ignore URL parse errors and fall back
  }
  
  return allowedOrigin;
}

/**
 * Build the CORS response headers for a given origin.
 * @param {string} origin
 * @returns {Record<string, string>}
 */
function buildCorsHeaders(origin) {
  return {
    "Access-Control-Allow-Origin": origin,
    "Access-Control-Allow-Methods": CORS_ALLOW_METHODS,
    "Access-Control-Allow-Headers": CORS_ALLOW_HEADERS,
    "Access-Control-Allow-Credentials": "true",
    "Access-Control-Max-Age": "86400",
  };
}

export default {
  /**
   * @param {Request} request
   * @param {{ UPSTREAM_URL: string; ALLOWED_ORIGIN: string }} env
   * @returns {Promise<Response>}
   */
  async fetch(request, env) {
    const allowedOrigin = env.ALLOWED_ORIGIN || "*";
    const corsOrigin = getCorsOrigin(request, allowedOrigin);

    // ── CORS Preflight ───────────────────────────────────────
    if (request.method === "OPTIONS") {
      return new Response(null, {
        status: 204,
        headers: buildCorsHeaders(corsOrigin),
      });
    }

    // ── Build upstream request ───────────────────────────────
    const url = new URL(request.url);
    const upstreamUrl = env.UPSTREAM_URL + url.pathname + url.search;

    // Copy request headers; remove hop-by-hop / Cloudflare-specific headers
    const upstreamHeaders = new Headers(request.headers);
    upstreamHeaders.delete("host");
    upstreamHeaders.delete("origin");
    upstreamHeaders.delete("cf-connecting-ip");
    upstreamHeaders.delete("cf-ipcountry");
    upstreamHeaders.delete("cf-ray");
    upstreamHeaders.delete("cf-visitor");

    const upstreamRequest = new Request(upstreamUrl, {
      method: request.method,
      headers: upstreamHeaders,
      body: request.method !== "GET" && request.method !== "HEAD" ? request.body : null,
      redirect: "follow",
    });

    // ── Proxy to Cloud Run ───────────────────────────────────
    let upstreamResponse;
    try {
      upstreamResponse = await fetch(upstreamRequest);
    } catch (err) {
      console.error("[proxy] upstream fetch failed:", err.message);
      return new Response(
        JSON.stringify({ error: "upstream_unavailable", message: err.message }),
        {
          status: 502,
          headers: {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": corsOrigin,
          },
        }
      );
    }

    // ── Build response with CORS headers ─────────────────────
    const responseHeaders = new Headers(upstreamResponse.headers);

    // Overwrite any upstream CORS headers with our own
    const corsHeaders = buildCorsHeaders(corsOrigin);
    for (const [key, value] of Object.entries(corsHeaders)) {
      responseHeaders.set(key, value);
    }

    return new Response(upstreamResponse.body, {
      status: upstreamResponse.status,
      statusText: upstreamResponse.statusText,
      headers: responseHeaders,
    });
  },
};
