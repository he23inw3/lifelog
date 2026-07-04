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

    // ── CORS Preflight ───────────────────────────────────────
    if (request.method === "OPTIONS") {
      return new Response(null, {
        status: 204,
        headers: buildCorsHeaders(allowedOrigin),
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
            "Access-Control-Allow-Origin": allowedOrigin,
          },
        }
      );
    }

    // ── Build response with CORS headers ─────────────────────
    const responseHeaders = new Headers(upstreamResponse.headers);

    // Overwrite any upstream CORS headers with our own
    const corsHeaders = buildCorsHeaders(allowedOrigin);
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
