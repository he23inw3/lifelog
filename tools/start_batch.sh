#!/usr/bin/env bash
# ============================================================
# LifeLog バッチ起動スクリプト（ローカル動作確認用）
#
# 使い方:
#   ./tools/start_batch.sh remind      # リマインドバッチ（RemindCheckExecutor）
#   ./tools/start_batch.sh reflection  # 月末振り返りバッチ（MonthlyReflectionExecutor）
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

log_warn() {
    echo -e "${YELLOW}[WARN]${RESET} $*"
}

log_error() {
    echo -e "${RED}[ERROR]${RESET} $*"
}

# ── パス・位置解決 ─────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
APP_DIR="${PROJECT_ROOT}/app/lifelog"
ENV_FILE="${SCRIPT_DIR}/.env"

# ── スクリプトの使い方表示 ─────────────────────────────────
usage() {
    echo -e "${BOLD}使い方:${RESET}"
    echo -e "  $0 remind [options]      # リマインドバッチ (RemindCheckExecutor)"
    echo -e "  $0 reflection [options]  # 月末振り返りバッチ (MonthlyReflectionExecutor)"
    echo -e ""
    echo -e "${BOLD}オプション:${RESET}"
    echo -e "  -b, --build             # 実行前にアプリケーションを強制ビルドする"
    exit 1
}

# ── 引数パース ──────────────────────────────────────────────
parse_arguments() {
    local batch_arg="${1:-}"
    local option_arg="${2:-}"

    case "${batch_arg}" in
        remind)
            QUARKUS_PROFILE="remind-batch"
            BATCH_LABEL="リマインドバッチ (remind-batch)"
            HTTP_PORT=9090
            ;;
        reflection)
            QUARKUS_PROFILE="reflection-batch"
            BATCH_LABEL="月末振り返りバッチ (reflection-batch)"
            HTTP_PORT=9091
            ;;
        *)
            log_error "有効なバッチ種別を指定してください。"
            usage
            ;;
    esac

    FORCE_BUILD=false
    if [[ "${option_arg}" == "--build" || "${option_arg}" == "-b" ]]; then
        FORCE_BUILD=true
    fi
}

# ── バナー表示 ──────────────────────────────────────────────
print_banner() {
    echo -e "${BOLD}${CYAN}"
    echo "╔══════════════════════════════════════════╗"
    printf "║  LifeLog Batch  %-26s║\n" "${BATCH_LABEL:0:24}"
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

# ── 環境変数設定 ───────────────────────────────────────────
setup_environment_variables() {
    export APP_NODE="${APP_NODE:-dev}"
    export QUARKUS_PROFILE
    export SLACK_SIGNING_SECRET="${SLACK_SIGNING_SECRET:-dev-signing-secret}"
    export SLACK_BOT_TOKEN="${SLACK_BOT_TOKEN:-xoxb-dev-token}"
    export GOOGLE_CLOUD_PROJECT="${GOOGLE_CLOUD_PROJECT:-lifelog-dev}"
    export BIGQUERY_DATASET="${BIGQUERY_DATASET:-lifelog_dev}"
    export BIGQUERY_TABLE="${BIGQUERY_TABLE:-daily_logs}"
    export GEMINI_LOCATION="${GEMINI_LOCATION:-asia-northeast1}"

    # Google Cloud Emulator & Credentials Overrides
    export QUARKUS_GOOGLE_CLOUD_SERVICE_ACCOUNT_LOCATION="${QUARKUS_GOOGLE_CLOUD_SERVICE_ACCOUNT_LOCATION:-${PROJECT_ROOT}/app/lifelog/src/main/resources/dummy-credentials.json}"
    export QUARKUS_GOOGLE_CLOUD_FIRESTORE_HOST_OVERRIDE="${QUARKUS_GOOGLE_CLOUD_FIRESTORE_HOST_OVERRIDE:-localhost:8080}"
    export QUARKUS_GOOGLE_CLOUD_BIGQUERY_HOST_OVERRIDE="${QUARKUS_GOOGLE_CLOUD_BIGQUERY_HOST_OVERRIDE:-http://localhost:9050}"

    # JVM オプション
    export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
}

# ── 起動情報表示 ───────────────────────────────────────────
print_runtime_info() {
    log_info "バッチ名          : ${BOLD}${BATCH_LABEL}${RESET}"
    log_info "プロファイル      : ${BOLD}${QUARKUS_PROFILE}${RESET}"
    log_info "GCP プロジェクト  : ${GOOGLE_CLOUD_PROJECT}"
    log_info "BigQuery テーブル : ${BIGQUERY_DATASET}.${BIGQUERY_TABLE}"
    echo ""
}

build_app_if_needed() {
    local jar_path="${APP_DIR}/build/quarkus-app/quarkus-run.jar"

    if [[ ! -f "${jar_path}" || "${FORCE_BUILD}" == "true" ]]; then
        cd "${APP_DIR}"
        log_info "LifeLog アプリケーションをビルドしています (テストスキップ)..."
        ./gradlew quarkusBuild -x test --console=plain -q
        echo ""
    else
        log_info "ビルドをスキップします（再ビルドして実行するには -b または --build オプションを付与してください）。"
    fi
}

# ── バッチJAR実行 ───────────────────────────────────────────
run_batch_jar() {
    local jar_path="${APP_DIR}/build/quarkus-app/quarkus-run.jar"

    if [[ ! -f "${jar_path}" ]]; then
        log_error "ビルド成果物が見つかりません: ${jar_path}"
        exit 1
    fi

    log_info "バッチを実行します: ${BATCH_LABEL}"
    echo ""

    exec java \
        -Dfile.encoding=UTF-8 \
        -Dquarkus.profile="${QUARKUS_PROFILE}" \
        -Dquarkus.http.port="${HTTP_PORT}" \
        -jar "${jar_path}"
}

# ── メイン実行フロー ────────────────────────────────────────
main() {
    if [[ $# -lt 1 ]]; then
        log_error "引数が不足しています。"
        usage
    fi

    parse_arguments "$1"
    print_banner
    load_env
    setup_environment_variables
    print_runtime_info
    build_app_if_needed
    run_batch_jar
}

main "$@"
