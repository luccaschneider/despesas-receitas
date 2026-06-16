#!/usr/bin/env bash
# ==============================================================================
# bootstrap_homolog.sh — VM zerada → dependências + clone + homologação (1 comando)
#
# Uso na apresentação (Ubuntu, com acesso à internet):
#   curl -fsSL https://raw.githubusercontent.com/luccaschneider/despesas-receitas/main/scripts/bootstrap_homolog.sh | sudo bash
#
# Variáveis opcionais (antes do comando):
#   REPO_URL, BRANCH, DB_PASSWORD
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]:-$0}")" && pwd)"
# Quando executado via pipe (curl | bash), BASH_SOURCE pode ser vazio; lib fica no clone.
LIB_PATH="${SCRIPT_DIR}/lib/deploy_common.sh"
if [[ ! -f "${LIB_PATH}" ]]; then
  LIB_PATH="/opt/app/homolog/scripts/lib/deploy_common.sh"
fi

if [[ -f "${LIB_PATH}" ]]; then
  # shellcheck source=scripts/lib/deploy_common.sh
  source "${LIB_PATH}"
else
  # Primeira execução via curl: funções mínimas embutidas até o clone trazer a lib completa.
  deploy_common::require_root() { [[ "$(id -u)" -eq 0 ]] || { echo "[ERRO] Use sudo." >&2; exit 1; }; }
  deploy_common::default_repo_url() { echo "${REPO_URL:-https://github.com/luccaschneider/despesas-receitas.git}"; }
  deploy_common::default_branch() { echo "${BRANCH:-main}"; }
  deploy_common::default_db_password() { echo "${DB_PASSWORD:-despesas_demo_2026}"; }
fi

deploy_common::require_root

REPO_URL="$(deploy_common::default_repo_url)"
BRANCH="$(deploy_common::default_branch)"
DB_PASSWORD="$(deploy_common::default_db_password)"
APP_DIR="/opt/app/homolog"

echo "========================================================"
echo "  Bootstrap: dependências + homologação"
echo "  Repositório: ${REPO_URL} (${BRANCH})"
echo "========================================================"

echo ""
echo "==> [1/5] Instalando pacotes base (Git, curl)..."
export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y git ca-certificates curl gnupg lsb-release

echo ""
echo "==> [2/5] Instalando Docker Engine e Compose..."
if ! command -v docker &> /dev/null; then
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
    | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  chmod a+r /etc/apt/keyrings/docker.gpg

  echo \
    "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
    https://download.docker.com/linux/ubuntu \
    $(lsb_release -cs) stable" \
    | tee /etc/apt/sources.list.d/docker.list > /dev/null

  apt-get update -y
  apt-get install -y \
    docker-ce \
    docker-ce-cli \
    containerd.io \
    docker-buildx-plugin \
    docker-compose-plugin

  systemctl enable docker
  systemctl start docker
else
  echo "    Docker já instalado: $(docker --version)"
fi

mkdir -p /opt/app/homolog /opt/app/prod

echo ""
echo "==> [3/5] Clonando repositório em ${APP_DIR}..."
if [[ ! -d "${APP_DIR}/.git" ]]; then
  git clone --branch "${BRANCH}" "${REPO_URL}" "${APP_DIR}"
else
  cd "${APP_DIR}"
  git fetch origin
  git checkout "${BRANCH}"
  git pull origin "${BRANCH}"
fi

# Após o clone, carrega a lib completa do repositório.
# shellcheck source=scripts/lib/deploy_common.sh
source "${APP_DIR}/scripts/lib/deploy_common.sh"

echo ""
echo "==> [4/5] Configurando ambiente de homologação..."
export POSTGRES_DB="homolog_db"
export POSTGRES_PORT="5432"
export DB_URL="jdbc:postgresql://db:5432/${POSTGRES_DB}"
export DB_USER="postgres"
export SERVER_PORT="8081"
export SPRING_PROFILES_ACTIVE="homolog"
deploy_common::print_email_defaults

deploy_common::write_env_file "${APP_DIR}"

echo "    SERVER_PORT            = ${SERVER_PORT}"
echo "    SPRING_PROFILES_ACTIVE = ${SPRING_PROFILES_ACTIVE}"
echo "    POSTGRES_DB            = ${POSTGRES_DB}"

echo ""
echo "==> [5/5] Subindo homologação (build pode levar alguns minutos)..."
cd "${APP_DIR}"
docker compose up -d --build

VM_IP="$(hostname -I 2>/dev/null | awk '{print $1}')"
VM_IP="${VM_IP:-<IP_DA_VM>}"

echo ""
echo "========================================================"
echo "  Homologação pronta!"
echo "  URL   : http://${VM_IP}:${SERVER_PORT}"
echo "  Login : admin"
echo "  Senha : admin123"
echo "  Logs  : docker compose -f ${APP_DIR}/docker-compose.yml logs -f app"
echo ""
echo "  Próximo passo (produção):"
echo "    sudo bash ${APP_DIR}/scripts/deploy_prod.sh"
echo "========================================================"
