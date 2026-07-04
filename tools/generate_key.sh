#!/usr/bin/env bash
# ============================================================
# 暗号化共通鍵 (CRYPTO_KEY) 生成スクリプト
#
# 使い方:
#   ./tools/generate_key.sh [オプション]
#
# オプション:
#   -w, --write     生成した鍵を tools/.env に書き込みます。
#   -f, --force     すでに CRYPTO_KEY が存在する場合でも強制的に上書きします。
#   -h, --help      ヘルプを表示します。
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

# ── ヘルプ表示 ──────────────────────────────────────────────
print_help() {
    echo "暗号化共通鍵 (CRYPTO_KEY) を生成するスクリプトです。"
    echo ""
    echo "使い方:"
    echo "  $0 [オプション]"
    echo ""
    echo "オプション:"
    echo "  -w, --write     生成した鍵を tools/.env に書き込みまたは更新します。"
    echo "  -f, --force     すでに CRYPTO_KEY が存在する場合でも強制的に上書きします。"
    echo "  -h, --help      このヘルプを表示します。"
    echo ""
}

# ── 鍵生成関数 ──────────────────────────────────────────────
generate_random_key() {
    # openssl が使える場合は openssl を使用し、使えない場合は /dev/urandom から生成する
    if command -v openssl >/dev/null 2>&1; then
        # 32バイトのランダムデータを16進数にした文字列 (64文字)
        openssl rand -hex 32
    else
        # フォールバック: 英数字のみの32文字のランダム文字列
        if [[ -e /dev/urandom ]]; then
            LC_ALL=C tr -dc 'A-Za-z0-9' < /dev/urandom | head -c 32
            echo ""
        else
            log_error "暗号学的に安全な乱数生成器 (openssl または /dev/urandom) が見つかりません。"
            exit 1
        fi
    fi
}

# ── メイン処理 ──────────────────────────────────────────────
main() {
    local write_mode=false
    local force_mode=false

    # 引数解析
    while [[ $# -gt 0 ]]; do
        case "$1" in
            -w|--write)
                write_mode=true
                shift
                ;;
            -f|--force)
                force_mode=true
                shift
                ;;
            -h|--help)
                print_help
                exit 0
                ;;
            *)
                log_error "無効なオプション: $1"
                print_help
                exit 1
                ;;
        esac
    done

    # 鍵の生成
    local new_key
    new_key=$(generate_random_key)

    if [[ "${write_mode}" = false ]]; then
        # 標準出力に表示するだけの場合
        echo -e "${BOLD}${CYAN}生成された暗号化共通鍵:${RESET}"
        echo -e "${GREEN}${new_key}${RESET}"
        echo ""
        log_info "この鍵を tools/.env に設定するには、以下のように実行してください:"
        log_info "  $0 --write"
        exit 0
    fi

    # tools/.env への書き込み処理
    log_info "tools/.env への書き込みを行います..."

    # .env ファイルが存在しない場合は新規作成
    if [[ ! -f "${ENV_FILE}" ]]; then
        log_info ".env ファイルを新規作成します: ${ENV_FILE}"
        touch "${ENV_FILE}"
    fi

    # 既存の CRYPTO_KEY があるか確認
    if grep -q "^CRYPTO_KEY=" "${ENV_FILE}"; then
        if [[ "${force_mode}" = false ]]; then
            log_warn ".env ファイルに既に CRYPTO_KEY が設定されています。"
            read -p "上書きしますか？ (y/N): " -r response
            if [[ ! "${response}" =~ ^[yY]$ ]]; then
                log_info "処理をキャンセルしました。"
                exit 0
            fi
        fi
        # 既存の行を置換する
        local tmp_file
        tmp_file=$(mktemp)
        sed -e "s/^CRYPTO_KEY=.*/CRYPTO_KEY=${new_key}/" "${ENV_FILE}" > "${tmp_file}"
        mv "${tmp_file}" "${ENV_FILE}"
        log_info "CRYPTO_KEY を更新しました。"
    else
        # 新規追加
        # ファイルの末尾に改行がない場合を考慮して、改行を挟んで追記する
        if [[ -s "${ENV_FILE}" && ! $(tail -c1 "${ENV_FILE}") == $'\n' ]]; then
            echo "" >> "${ENV_FILE}"
        fi
        echo "CRYPTO_KEY=${new_key}" >> "${ENV_FILE}"
        log_info "CRYPTO_KEY を .env に追加しました。"
    fi

    log_info "設定完了: CRYPTO_KEY=${new_key}"
}

main "$@"
