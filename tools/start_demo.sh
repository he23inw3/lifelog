#!/usr/bin/env bash
# ============================================================
# LifeLog Demo Frontend 起動スクリプト（ローカル開発用）
#
# 使い方:
#   ./tools/start_demo.sh
# ============================================================

set -euo pipefail

# ── カラー定義 ─────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; BOLD='\033[1m'; RESET='\033[0m'

# ── ログ関数 ────────────────────────────────────────────────
log_info() {
    echo -e "${GREEN}[INFO]${RESET} $*"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${RESET} $*"
}

log_error() {
    echo -e "${RED}[ERROR]${RESET} $*"
}

# ── パス・位置解決 ─────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
DEMO_DIR="${PROJECT_ROOT}/app/lifelog-demo"
ENV_FILE="${SCRIPT_DIR}/.env"

# ── バナー表示 ──────────────────────────────────────────────
print_banner() {
    echo -e "${BOLD}${CYAN}"
    echo "╔══════════════════════════════════════════╗"
    echo "║     LifeLog Demo Frontend (Vite dev)     ║"
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
    fi
}

# ── 開発用デフォルト環境変数設定 ───────────────────────────
setup_environment_variables() {
    export VITE_API_BASE_URL="${VITE_API_BASE_URL:-http://localhost:5000}"
}

# ── 依存ライブラリのインストール確認 ─────────────────────────
ensure_dependencies() {
    if [[ ! -d "${DEMO_DIR}/node_modules" ]]; then
        log_info "node_modules が見つからないため、依存関係をインストールします..."
        cd "${DEMO_DIR}"
        npm install
    fi
}

# ── 起動情報表示 ───────────────────────────────────────────
print_runtime_info() {
    log_info "接続先 API URL : ${BOLD}${VITE_API_BASE_URL}${RESET}"
    echo ""
    log_info "Starting Vite Development Server..."
    echo -e "    App URL: ${BOLD}http://localhost:5173${RESET}"
    echo ""
}

# ── Vite による起動 ──────────────────────────────────────────
start_vite_dev() {
    cd "${DEMO_DIR}"
    exec npm run dev
}

# ── メイン実行フロー ────────────────────────────────────────
main() {
    print_banner
    load_env
    setup_environment_variables
    ensure_dependencies
    print_runtime_info
    start_vite_dev
}

main "$@"
