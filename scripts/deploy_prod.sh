#!/usr/bin/env bash
# ==============================================================================
# deploy_prod.sh — Deploy de produção via Ansible
# Uso: sudo bash scripts/deploy_prod.sh
#      sudo bash /opt/app/homolog/scripts/deploy_prod.sh   (após bootstrap)
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/lib/deploy_common.sh
source "${SCRIPT_DIR}/lib/deploy_common.sh"

deploy_common::require_root

REPO_URL="$(deploy_common::default_repo_url)"
BRANCH="$(deploy_common::default_branch)"
DB_PASSWORD="$(deploy_common::default_db_password)"
APP_DIR="/opt/app/prod"
SERVER_PORT="8082"
ANSIBLE_ROOT="$(deploy_common::resolve_ansible_root "${SCRIPT_DIR}")"
ANSIBLE_INVENTORY="$(deploy_common::default_ansible_inventory)"

deploy_common::validate_repo_url "${REPO_URL}"

echo "==> Deploy de PRODUÇÃO via Ansible"
echo "    Diretório da aplicação: ${APP_DIR}"
deploy_common::run_ansible_deploy \
  "prod" \
  "${ANSIBLE_ROOT}" \
  "${ANSIBLE_INVENTORY}" \
  "${REPO_URL}" \
  "${BRANCH}" \
  "${DB_PASSWORD}"

deploy_common::print_deploy_success "prod" "${SERVER_PORT}" "${APP_DIR}"
