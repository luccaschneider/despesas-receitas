#!/usr/bin/env bash
# ==============================================================================
# deploy_homolog.sh — Deploy de homologação via Ansible
# Uso: sudo bash scripts/deploy_homolog.sh
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/lib/deploy_common.sh
source "${SCRIPT_DIR}/lib/deploy_common.sh"

deploy_common::require_root

REPO_URL="$(deploy_common::default_repo_url)"
BRANCH="$(deploy_common::default_branch)"
DB_PASSWORD="$(deploy_common::default_db_password)"
APP_DIR="/opt/app/homolog"
SERVER_PORT="8081"
ANSIBLE_ROOT="$(deploy_common::resolve_ansible_root "${SCRIPT_DIR}")"
ANSIBLE_INVENTORY="$(deploy_common::default_ansible_inventory)"

deploy_common::validate_repo_url "${REPO_URL}"

echo "==> Deploy de HOMOLOGAÇÃO via Ansible"
echo "    Diretório da aplicação: ${APP_DIR}"
deploy_common::run_ansible_deploy \
  "homolog" \
  "${ANSIBLE_ROOT}" \
  "${ANSIBLE_INVENTORY}" \
  "${REPO_URL}" \
  "${BRANCH}" \
  "${DB_PASSWORD}"

deploy_common::print_deploy_success "homolog" "${SERVER_PORT}" "${APP_DIR}"
