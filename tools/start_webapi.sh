#!/usr/bin/env bash
# ============================================================
# LifeLog API 起動スクリプト（ローカル開発用）
#
# 使い方:
#   ./tools/start_webapi.sh [--demo|--prod]
#     --demo: デモモードで起動 (デフォルト)
#     --prod: 本番モード (デモモード無効) で起動
#
# 前提:
#   - tools/.env ファイルに機密環境変数を定義してください（任意）
#   - GCP Application Default Credentials が設定済みであること
#     $ gcloud auth application-default login
# ============================================================

set -euo pipefail

# ── カラー定義 ─────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; BOLD='\033[1m'; RESET='\033[0m'

# ── ログ関数 ────────────────────────────────────────────────
log_info() {
    echo -e "${GREEN}[INFO]${RESET} $*"
}

# ── ログ関数 (警告) ──────────────────────────────────────────
log_warn() {
    echo -e "${YELLOW}[WARN]${RESET} $*"
}

# ── ログ関数 (エラー) ──────────────────────────────────────────
log_error() {
    echo -e "${RED}[ERROR]${RESET} $*"
}

# ── パス・位置解決 ─────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
APP_DIR="${PROJECT_ROOT}/app/lifelog"
ENV_FILE="${SCRIPT_DIR}/.env"

# デモモードフラグ（デフォルト: true）
DEMO_MODE="true"

# ── 引数解析 ──────────────────────────────────────────────
parse_args() {
    while [[ $# -gt 0 ]]; do
        case "$1" in
            --demo)
                DEMO_MODE="true"
                shift
                ;;
            --prod|--real)
                DEMO_MODE="false"
                shift
                ;;
            *)
                log_error "無効なオプションです: $1"
                echo "使い方: ./tools/start_webapi.sh [--demo|--prod]"
                exit 1
                ;;
                esac
    done
}

# ── バナー表示 ──────────────────────────────────────────────
print_banner() {
    echo -e "${BOLD}${CYAN}"
    echo "╔══════════════════════════════════════════╗"
    echo "║         LifeLog API   (dev mode)         ║"
    echo "╚══════════════════════════════════════════╝"
    echo -e "${RESET}"
}

# ── .env ロード ─────────────────────────────────────────────
load_env() {
    if [[ -f "${ENV_FILE}" ]]; then
        log_info ".env を読み込みます: ${ENV_FILE}"
        set -a
        # shellcheck disable=SC1090
        source "${ENV_FILE}"
        set +a
    else
        log_warn ".env が見つかりません（${ENV_FILE}）"
        echo -e "       機密値は環境変数として手動設定するか .env を作成してください。"
    fi
}

# ── 開発用デフォルト環境変数設定 ───────────────────────────
setup_environment_variables() {
    export APP_NODE="${APP_NODE:-dev}"
    export QUARKUS_PROFILE="${APP_NODE}"
    export DEMO_MODE="${DEMO_MODE}"
    export SLACK_SIGNING_SECRET="${SLACK_SIGNING_SECRET:-dev-signing-secret}"
    export SLACK_BOT_TOKEN="${SLACK_BOT_TOKEN:-xoxb-dev-token}"
    export GOOGLE_CLOUD_PROJECT="${GOOGLE_CLOUD_PROJECT:-lifelog-dev}"
    export BIGQUERY_DATASET="${BIGQUERY_DATASET:-lifelog_dev}"
    export BIGQUERY_TABLE="${BIGQUERY_TABLE:-daily_logs}"
    export GEMINI_LOCATION="${GEMINI_LOCATION:-us-central1}"
    export OIDC_CLIENT_ID="${OIDC_CLIENT_ID:-lifelog}"

    # Google Cloud Emulator & Credentials Overrides
    export GOOGLE_GENAI_USE_VERTEXAI="true"
    export GOOGLE_CLOUD_LOCATION="${GEMINI_LOCATION:-asia-northeast1}"
    export QUARKUS_GOOGLE_CLOUD_SERVICE_ACCOUNT_LOCATION="${QUARKUS_GOOGLE_CLOUD_SERVICE_ACCOUNT_LOCATION:-${PROJECT_ROOT}/app/lifelog/src/main/resources/dummy-credentials.json}"
    export QUARKUS_GOOGLE_CLOUD_FIRESTORE_HOST_OVERRIDE="${QUARKUS_GOOGLE_CLOUD_FIRESTORE_HOST_OVERRIDE:-localhost:8080}"
    export QUARKUS_GOOGLE_CLOUD_BIGQUERY_HOST_OVERRIDE="${QUARKUS_GOOGLE_CLOUD_BIGQUERY_HOST_OVERRIDE:-http://localhost:9050}"

    # JVM オプション (WindowsのGit Bash等での文字化けを防ぐため、コンソール出力に UTF-8 を強制設定)
    export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8"
}

# ── 起動情報表示 ───────────────────────────────────────────
print_runtime_info() {
    log_info "プロファイル      : ${BOLD}${QUARKUS_PROFILE}${RESET}"
    log_info "起動モード        : ${BOLD}$( [[ "${DEMO_MODE}" == "true" ]] && echo "デモモード (DEMO)" || echo "本番モード (REAL)" )${RESET}"
    log_info "GCP プロジェクト  : ${GOOGLE_CLOUD_PROJECT}"
    log_info "BigQuery テーブル : ${BIGQUERY_DATASET}.${BIGQUERY_TABLE}"
    log_info "Gemini ゾーン     : ${GEMINI_LOCATION}"
    echo ""
    log_info "Starting LifeLog API (Quarkus Dev Mode)..."
    echo -e "    Swagger UI: ${BOLD}http://localhost:5000/swagger-ui${RESET}"
    echo ""
}

# ── Gradle による起動 ───────────────────────────────────────
start_quarkus_dev() {
    cd "${APP_DIR}"
    exec ./gradlew quarkusDev \
        --console=rich
}

# ── メイン実行フロー ────────────────────────────────────────
main() {
    parse_args "$@"
    print_banner
    load_env
    setup_environment_variables
    print_runtime_info
    start_quarkus_dev
}

main "$@"
