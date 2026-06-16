#!/usr/bin/env bash
# ==============================================================================
# deploy_prod.sh — Deploy de produção
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

deploy_common::validate_repo_url "${REPO_URL}"

# POSTGRES_PORT no host diferente de homolog para os dois rodarem na mesma VM
export POSTGRES_DB="prod_db"
export POSTGRES_PORT="5433"
export DB_URL="jdbc:postgresql://db:5432/${POSTGRES_DB}"
export DB_USER="postgres"
export SERVER_PORT="80"
export SPRING_PROFILES_ACTIVE="prod"
deploy_common::print_email_defaults

echo "==> [1/4] Diretório: ${APP_DIR}"
deploy_common::clone_or_update_repo "${REPO_URL}" "${BRANCH}" "${APP_DIR}"

echo "==> [2/4] Gerando ${APP_DIR}/.env"
deploy_common::write_env_file "${APP_DIR}"

echo "==> [3/4] Variáveis principais:"
echo "    SERVER_PORT            = ${SERVER_PORT}"
echo "    SPRING_PROFILES_ACTIVE = ${SPRING_PROFILES_ACTIVE}"
echo "    POSTGRES_DB            = ${POSTGRES_DB}"
echo "    POSTGRES_PORT (host)   = ${POSTGRES_PORT}"

echo "==> [4/4] Subindo contêineres..."
cd "${APP_DIR}"
docker compose up -d --build

VM_IP="$(hostname -I 2>/dev/null | awk '{print $1}')"
VM_IP="${VM_IP:-<IP_DA_VM>}"

echo ""
echo "========================================================"
echo "  Deploy de PRODUÇÃO concluído!"
echo "  URL   : http://${VM_IP}"
echo "  Login : admin / admin123"
echo "  Logs  : docker compose -f ${APP_DIR}/docker-compose.yml logs -f app"
echo "========================================================"
