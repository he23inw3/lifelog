#!/usr/bin/env bash
# ============================================================
# LifeLog テストデータ登録スクリプト
#
# 使い方:
#   ./tools/seed.sh
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
ENV_FILE="${SCRIPT_DIR}/.env"

# ── .env ロード & デフォルト環境変数設定 ───────────────────
load_env_and_defaults() {
    if [[ -f "${ENV_FILE}" ]]; then
        log_info ".env を読み込みます: ${ENV_FILE}"
        set -a
        # shellcheck disable=SC1090
        source "${ENV_FILE}"
        set +a
    else
        log_warn ".env が見つかりません（${ENV_FILE}）。デフォルト値を使用します。"
    fi

    # デフォルト設定値
    export GOOGLE_CLOUD_PROJECT="${GOOGLE_CLOUD_PROJECT:-lifelog-dev}"
    export BIGQUERY_DATASET="${BIGQUERY_DATASET:-lifelog_dev}"
    BIGQUERY_TABLE="daily_logs"

    # Firestore コレクション名（定数定義）
    FIRESTORE_USER_SETTINGS_COLLECTION="user_settings"
    FIRESTORE_USER_CREDENTIALS_COLLECTION="user_credentials"
    FIRESTORE_ADMIN_USERS_COLLECTION="admin_users"
    FIRESTORE_DEMO_CALENDAR_COLLECTION="demo_calendar_events"
    FIRESTORE_DEMO_SLACK_COLLECTION="demo_slack_messages"
    export FIRESTORE_EMULATOR_HOST="${FIRESTORE_EMULATOR_HOST:-localhost:8080}"
    export BIGQUERY_EMULATOR_HOST="${BIGQUERY_EMULATOR_HOST:-http://localhost:9050}"
}

# ── Firestore接続用共通関数 ──────────────────────────────
curl_firestore() {
    local method="$1"
    local url="$2"
    local data="$3"

    local res
    local http_code
    res=$(printf "%s" "${data}" | curl -s -w "%{http_code}" --connect-timeout 5 -X "${method}" "${headers[@]}" -d @- "${url}")
    http_code="${res: -3}"
    local body="${res:0:${#res}-3}"

    if [[ "${http_code}" -lt 200 || "${http_code}" -ge 300 ]]; then
        log_error "Firestore 登録エラー (HTTP ${http_code}):"
        echo "${body}" >&2
        return 22
    fi
}

# ── Firestore テストデータ登録 (REST API経由) ──────────────
seed_firestore() {
    log_info "Firestore テストデータを登録します..."

    # エミュレータホストの判定
    local host=""
    local headers=("-H" "Content-Type: application/json; charset=utf-8")
    local project_id="lifelog-dev"

    if [[ -n "${FIRESTORE_EMULATOR_HOST:-}" ]]; then
        host="${FIRESTORE_EMULATOR_HOST}"
        if [[ ! "${host}" =~ ^https?:// ]]; then
            host="http://${host}"
        fi
        log_info "Firestore エミュレータを使用します: ${host} (Project: ${project_id})"
    else
        # 実機の場合はアクセストークンを取得
        if ! command -v gcloud &> /dev/null; then
            log_error "gcloud CLI が見つかりません。実機 Firestore に登録するには gcloud が必要です。"
            return 1
        fi
        local token
        token=$(gcloud auth print-access-token)
        host="https://firestore.googleapis.com"
        headers+=("-H" "Authorization: Bearer ${token}")
        project_id="${GOOGLE_CLOUD_PROJECT}"
        log_info "実機 Firestore API を使用します: ${host} (Project: ${project_id})"
    fi

    local base_url="${host}/v1/projects/${project_id}/databases/(default)/documents"

    # 1. 管理者データ登録 (admin_users)
    local admin_email="admin@example.com"
    log_info "管理者データを登録中 (${admin_email})..."
    local admin_json
    admin_json=$(cat <<EOF
{
  "fields": {
    "email": { "stringValue": "${admin_email}" },
    "userName": { "stringValue": "開発者管理者" },
    "isActive": { "booleanValue": true },
    "createdBy": { "stringValue": "system-bootstrap" },
    "createdAt": { "integerValue": "1780838400" },
    "updatedBy": { "stringValue": "system-bootstrap" },
    "updatedAt": { "integerValue": "1780838400" }
  }
}
EOF
)
    curl_firestore PATCH "${base_url}/${FIRESTORE_ADMIN_USERS_COLLECTION}/${admin_email}" "${admin_json}"

    # 2. ユーザー設定データ登録 (user_settings - テストユーザーA)
    local slack_user_id="U12345678"
    log_info "ユーザー設定データを登録中 (${slack_user_id})..."
    local setting_json
    setting_json=$(cat <<EOF
{
  "fields": {
    "slackUserId": { "stringValue": "${slack_user_id}" },
    "userName": { "stringValue": "テストユーザーA" },
    "remindTime": { "stringValue": "22:00" },
    "isActive": { "booleanValue": true },
    "createdAt": { "integerValue": "1780838400" },
    "updatedAt": { "integerValue": "1780838400" },
    "slack_user_id": { "stringValue": "${slack_user_id}" },
    "user_name": { "stringValue": "テストユーザーA" },
    "remind_time": { "stringValue": "22:00" },
    "is_active": { "booleanValue": true },
    "created_at": { "stringValue": "2026-06-06T00:00:00Z" }
  }
}
EOF
)
    curl_firestore PATCH "${base_url}/${FIRESTORE_USER_SETTINGS_COLLECTION}/${slack_user_id}" "${setting_json}"

    # 2b. ユーザー認証情報登録 (user_credentials - テストユーザーA)
    log_info "ユーザー認証データを登録中 (${slack_user_id})..."
    local credentials_json
    credentials_json=$(cat <<EOF
{
  "fields": {
    "slackUserId": { "stringValue": "${slack_user_id}" },
    "googleEmail": { "stringValue": "test@example.com" },
    "googleCalendarId": { "stringValue": "test-calendar@gmail.com" },
    "encryptedRefreshToken": { "stringValue": "4IdHK+hurLz2ht9Bai7jwWcEa/Bf9RHC3RE5N1hVNS+qnmzL18KmNHXcEcE/KIqR" }
  }
}
EOF
)
    curl_firestore PATCH "${base_url}/${FIRESTORE_USER_CREDENTIALS_COLLECTION}/${slack_user_id}" "${credentials_json}"

    # 3. デモユーザー設定登録 (user_settings - DEMO_USER)
    local demo_user_id="DEMO_USER"
    log_info "デモユーザー設定データを登録中 (${demo_user_id})..."
    local demo_setting_json
    demo_setting_json=$(cat <<EOF
{
  "fields": {
    "slackUserId": { "stringValue": "${demo_user_id}" },
    "userName": { "stringValue": "デモユーザー" },
    "remindTime": { "stringValue": "18:00" },
    "isActive": { "booleanValue": true },
    "createdAt": { "integerValue": "1780838400" },
    "updatedAt": { "integerValue": "1780838400" },
    "slack_user_id": { "stringValue": "${demo_user_id}" },
    "user_name": { "stringValue": "デモユーザー" },
    "remind_time": { "stringValue": "18:00" },
    "is_active": { "booleanValue": true },
    "created_at": { "stringValue": "2026-06-06T00:00:00Z" }
  }
}
EOF
)
    curl_firestore PATCH "${base_url}/${FIRESTORE_USER_SETTINGS_COLLECTION}/${demo_user_id}" "${demo_setting_json}"

    # 3b. デモユーザー認証情報登録 (user_credentials - DEMO_USER)
    log_info "デモユーザー認証データを登録中 (${demo_user_id})..."
    local demo_credentials_json
    demo_credentials_json=$(cat <<EOF
{
  "fields": {
    "slackUserId": { "stringValue": "${demo_user_id}" },
    "googleEmail": { "stringValue": "demo@example.com" },
    "googleCalendarId": { "stringValue": "demo@example.com" },
    "encryptedRefreshToken": { "stringValue": "4IdHK+hurLz2ht9Bai7jwWcEa/Bf9RHC3RE5N1hVNS+qnmzL18KmNHXcEcE/KIqR" }
  }
}
EOF
)
    curl_firestore PATCH "${base_url}/${FIRESTORE_USER_CREDENTIALS_COLLECTION}/${demo_user_id}" "${demo_credentials_json}"

    # 4. デモカレンダーイベントデータ登録 (demo_calendar_events)
    log_info "デモカレンダーイベントデータを登録中..."
    local events=(
        "demo_example_com_2026-06-01:2026-06-01:有給休暇:終日お休みです。:true"
        "demo_example_com_2026-06-02:2026-06-02:Quarkus & React 開発:10-00-19-00 設計および実装:false"
        "demo_example_com_2026-06-03:2026-06-03:CORS設定修正テスト:13-00-15-00 接続確認テスト:false"
    )

    for event in "${events[@]}"; do
        IFS=":" read -r doc_id ev_date ev_title ev_desc ev_holiday <<< "${event}"
        # コロンが含まれる説明文のハイフン復元
        local clean_desc
        clean_desc=$(echo "${ev_desc}" | tr '-' ':')
        local event_json
        event_json=$(cat <<EOF
{
  "fields": {
    "calendarId": { "stringValue": "demo@example.com" },
    "date": { "stringValue": "${ev_date}" },
    "title": { "stringValue": "${ev_title}" },
    "description": { "stringValue": "${clean_desc}" },
    "holiday": { "booleanValue": ${ev_holiday} },
    "syncedAt": { "integerValue": "1780838400" }
  }
}
EOF
)
        curl_firestore PATCH "${base_url}/${FIRESTORE_DEMO_CALENDAR_COLLECTION}/${doc_id}" "${event_json}"
    done

    # 5. デモSlackメッセージデータ登録 (demo_slack_messages)
    log_info "デモSlackメッセージデータを登録中..."
    local slack_messages=(
        "DEMO_USER:POST:【リマインド】本日の日報が未入力です。登録をお願いします。"
        "DEMO_USER:POST:【リマインド】夜遅く失礼します。今日一日どうでした？稼働時間と作業内容を教えてください！"
    )

    for msg in "${slack_messages[@]}"; do
        IFS=":" read -r msg_user msg_type msg_text <<< "${msg}"
        # コロンのハイフン復元
        local clean_text
        clean_text=$(echo "${msg_text}" | tr '-' ':')
        local msg_json
        msg_json=$(cat <<EOF
{
  "fields": {
    "slackUserId": { "stringValue": "${msg_user}" },
    "type": { "stringValue": "${msg_type}" },
    "text": { "stringValue": "${clean_text}" },
    "timestamp": { "integerValue": "1780838400" }
  }
}
EOF
)
        curl_firestore POST "${base_url}/${FIRESTORE_DEMO_SLACK_COLLECTION}" "${msg_json}"
    done

    log_info "Firestore のテストデータ登録が完了しました。"
}

# ── BigQuery テストデータ登録 (REST API経由) ───────────────
seed_bigquery() {
    log_info "BigQuery テストデータを登録します..."

    # エミュレータホストの判定
    local host=""
    local headers=("-H" "Content-Type: application/json; charset=utf-8")
    local project_id="lifelog-dev"

    if [[ -n "${BIGQUERY_EMULATOR_HOST:-}" ]]; then
        host="${BIGQUERY_EMULATOR_HOST}"
        log_info "BigQuery エミュレータを使用します: ${host} (Project: ${project_id})"
    else
        # 実機の場合はアクセストークンを取得
        if ! command -v gcloud &> /dev/null; then
            log_error "gcloud CLI が見つかりません。実機 BigQuery に登録するには gcloud が必要です。"
            return 1
        fi
        local token
        token=$(gcloud auth print-access-token)
        host="https://bigquery.googleapis.com"
        headers+=("-H" "Authorization: Bearer ${token}")
        project_id="${GOOGLE_CLOUD_PROJECT}"
        log_info "実機 BigQuery API を使用します: ${host} (Project: ${project_id})"
    fi

    local base_url="${host}/bigquery/v2/projects/${project_id}"

    # 冪等性を担保するため、既存の対象テストデータを削除してから挿入する（エミュレータがMERGEのAND条件をサポートしていないための回避策）
    local delete_query="DELETE FROM \`${project_id}.${BIGQUERY_DATASET}.${BIGQUERY_TABLE}\` WHERE slack_user_id IN ('U12345678', 'DEMO_USER')"
    
    local insert_query
    insert_query=$(cat <<EOF
INSERT INTO \`${project_id}.${BIGQUERY_DATASET}.${BIGQUERY_TABLE}\` (slack_user_id, log_date, raw_text, is_holiday, tasks, work_hours, overtime_hours, diary, sentiment, trace_id, created_at, updated_at)
VALUES
  ('U12345678', DATE('2026-06-01'), '今日はQuarkusの開発をしました。', false, 'Quarkusアプリ開発', CAST(8.0 AS FLOAT), CAST(1.0 AS FLOAT), '開発が捗った。', 'Happy', 'trace-001', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
  ('U12345678', DATE('2026-06-02'), '本日は有給休暇を取得しました。', true, '', CAST(0.0 AS FLOAT), CAST(0.0 AS FLOAT), '温泉に行ってリフレッシュ。', 'Happy', 'trace-002', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
  ('U12345678', DATE('2026-06-03'), 'Slack署名検証の実装。バグ修正で苦戦。', false, 'Slack API署名検証', CAST(7.5 AS FLOAT), CAST(0.0 AS FLOAT), 'エラーを解決して動いた！', 'Neutral', 'trace-003', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
  ('DEMO_USER', DATE('2026-06-01'), '本日は有給休暇を取得しました。終日お休みです。', true, '', CAST(0.0 AS FLOAT), CAST(0.0 AS FLOAT), '終日お休みです。', 'Happy', 'demo-trace-001', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
  ('DEMO_USER', DATE('2026-06-02'), '10時から19時までQuarkusとReactで開発を行いました。設計および実装を進めました。', false, 'Quarkus & React 開発', CAST(8.0 AS FLOAT), CAST(1.0 AS FLOAT), '設計および実装が順調に進みました。', 'Happy', 'demo-trace-002', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
  ('DEMO_USER', DATE('2026-06-03'), '13時から15時までCORS設定修正テストを行い、接続確認テストを完了しました。', false, 'CORS設定修正テスト', CAST(2.0 AS FLOAT), CAST(0.0 AS FLOAT), '接続確認テストを行いました。', 'Neutral', 'demo-trace-003', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP())
EOF
)

    # 1. 既存データの削除
    log_info "BigQuery から既存の重複テストデータを削除中..."
    local escaped_delete
    escaped_delete=$(echo "${delete_query}" | tr '\n' ' ' | sed 's/"/\\"/g')
    local delete_json="{\"query\": \"${escaped_delete}\", \"useLegacySql\": false}"

    local res
    local http_code
    res=$(printf "%s" "${delete_json}" | curl -s -w "%{http_code}" --connect-timeout 10 -X POST "${headers[@]}" -d @- "${base_url}/queries")
    http_code="${res: -3}"
    local body="${res:0:${#res}-3}"

    if [[ "${http_code}" -ge 200 && "${http_code}" -lt 300 ]]; then
        if echo "${body}" | grep -q '"errors"'; then
            log_error "BigQuery 既存データ削除中にエラーが発生しました:"
            echo "${body}" >&2
            return 1
        fi
    else
        log_error "BigQuery APIへの接続に失敗しました (DELETE, HTTP ${http_code}):"
        echo "${body}" >&2
        return 1
    fi

    # 2. テストデータの挿入
    log_info "BigQuery にテストデータを挿入中..."
    local escaped_insert
    escaped_insert=$(echo "${insert_query}" | tr '\n' ' ' | sed 's/"/\\"/g')
    local insert_json="{\"query\": \"${escaped_insert}\", \"useLegacySql\": false}"

    res=$(printf "%s" "${insert_json}" | curl -s -w "%{http_code}" --connect-timeout 10 -X POST "${headers[@]}" -d @- "${base_url}/queries")
    http_code="${res: -3}"
    body="${res:0:${#res}-3}"

    if [[ "${http_code}" -ge 200 && "${http_code}" -lt 300 ]]; then
        if echo "${body}" | grep -q '"errors"'; then
            log_error "BigQuery テストデータ挿入中にエラーが発生しました:"
            echo "${body}" >&2
            return 1
        else
            log_info "BigQuery のテストデータ登録が完了しました。"
        fi
    else
        log_error "BigQuery APIへの接続に失敗しました (INSERT, HTTP ${http_code}):"
        echo "${body}" >&2
        return 1
    fi
}

# ── メイン実行フロー ────────────────────────────────────────
main() {
    echo -e "${BOLD}${CYAN}"
    echo "╔══════════════════════════════════════════╗"
    echo "║       LifeLog Seed Test Data Script      ║"
    echo "╚══════════════════════════════════════════╝"
    echo -e "${RESET}"

    load_env_and_defaults
    seed_firestore
    seed_bigquery

    echo ""
    log_info "すべてのテストデータ初期化処理が完了しました！"
}

main "$@"
