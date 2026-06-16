#!/usr/bin/env bash
# ==============================================================================
# bootstrap_homolog.sh — VM zerada → dependências + Ansible + homologação (1 comando)
#
# Uso na apresentação (Ubuntu, com acesso à internet):
#   curl -fsSL https://raw.githubusercontent.com/luccaschneider/despesas-receitas/main/scripts/bootstrap_homolog.sh | sudo bash
#
# Variáveis opcionais (antes do comando):
#   REPO_URL, BRANCH, DB_PASSWORD, ANSIBLE_INVENTORY, BOOTSTRAP_REPO_DIR
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]:-$0}")" && pwd)"
LIB_PATH="${SCRIPT_DIR}/lib/deploy_common.sh"
if [[ ! -f "${LIB_PATH}" ]]; then
  LIB_PATH="/opt/despesas-receitas/scripts/lib/deploy_common.sh"
fi

if [[ -f "${LIB_PATH}" ]]; then
  # shellcheck source=scripts/lib/deploy_common.sh
  source "${LIB_PATH}"
else
  deploy_common::require_root() { [[ "$(id -u)" -eq 0 ]] || { echo "[ERRO] Use sudo." >&2; exit 1; }; }
  deploy_common::default_repo_url() { echo "${REPO_URL:-https://github.com/luccaschneider/despesas-receitas.git}"; }
  deploy_common::default_branch() { echo "${BRANCH:-main}"; }
  deploy_common::default_db_password() { echo "${DB_PASSWORD:-despesas_demo_2026}"; }
  deploy_common::default_ansible_inventory() { echo "${ANSIBLE_INVENTORY:-ansible/inventory/hosts.yml}"; }
  deploy_common::default_bootstrap_repo_dir() { echo "${BOOTSTRAP_REPO_DIR:-/opt/despesas-receitas}"; }
  deploy_common::ensure_ansible_installed() {
    if command -v ansible-playbook &> /dev/null; then
      echo "    Ansible ja instalado: $(ansible-playbook --version | head -n1)"
      return 0
    fi
    echo "    Instalando Ansible..."
    export DEBIAN_FRONTEND=noninteractive
    apt-get update -y
    apt-get install -y ansible
  }
  deploy_common::clone_bootstrap_repo() {
    local repo_url="$1"
    local branch="$2"
    local repo_dir="$3"
    mkdir -p "$(dirname "${repo_dir}")"
    if [[ ! -d "${repo_dir}/.git" ]]; then
      echo "    Clonando repositorio em ${repo_dir}..."
      git clone --branch "${branch}" --depth 1 "${repo_url}" "${repo_dir}"
      return 0
    fi
    echo "    Repositorio ja existe em ${repo_dir} — atualizando..."
    git -C "${repo_dir}" fetch --depth 1 origin "${branch}"
    git -C "${repo_dir}" checkout "${branch}"
    git -C "${repo_dir}" pull --ff-only origin "${branch}"
  }
  deploy_common::run_ansible_deploy() {
    local environment="$1"
    local ansible_root="$2"
    local inventory="${3:-$(deploy_common::default_ansible_inventory)}"
    local repo_url="${4:-$(deploy_common::default_repo_url)}"
    local branch="${5:-$(deploy_common::default_branch)}"
    local db_password="${6:-$(deploy_common::default_db_password)}"
    (
      cd "${ansible_root}"
      ansible-playbook \
        -i "${inventory}" \
        ansible/playbooks/deploy.yml \
        -l "${environment}" \
        -e "repo_url=${repo_url}" \
        -e "branch=${branch}" \
        -e "db_password=${db_password}"
    )
  }
  deploy_common::print_deploy_success() {
    local server_port="$2"
    local app_dir="$3"
    local vm_ip
    vm_ip="$(hostname -I 2>/dev/null | awk '{print $1}')"
    vm_ip="${vm_ip:-<IP_DA_VM>}"
    echo ""
    echo "========================================================"
    echo "  Homologacao pronta!"
    echo "  URL   : http://${vm_ip}:${server_port}"
    echo "  Login : admin"
    echo "  Senha : admin123"
    echo "  Logs  : docker compose -f ${app_dir}/docker-compose.yml logs -f app"
    echo ""
    echo "  Proximo passo (producao):"
    echo "    sudo bash ${app_dir}/scripts/deploy_prod.sh"
    echo "========================================================"
  }
fi

deploy_common::require_root

REPO_URL="$(deploy_common::default_repo_url)"
BRANCH="$(deploy_common::default_branch)"
DB_PASSWORD="$(deploy_common::default_db_password)"
BOOTSTRAP_REPO_DIR="$(deploy_common::default_bootstrap_repo_dir)"
ANSIBLE_INVENTORY="$(deploy_common::default_ansible_inventory)"
APP_DIR="/opt/app/homolog"
SERVER_PORT="8081"

echo "========================================================"
echo "  Bootstrap: dependências + Ansible + homologação"
echo "  Repositório: ${REPO_URL} (${BRANCH})"
echo "========================================================"

echo ""
echo "==> [1/5] Instalando pacotes base (Git, curl, Ansible)..."
export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y git ca-certificates curl gnupg lsb-release
deploy_common::ensure_ansible_installed

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
echo "==> [3/5] Obtendo repositório para os playbooks Ansible..."
deploy_common::clone_bootstrap_repo "${REPO_URL}" "${BRANCH}" "${BOOTSTRAP_REPO_DIR}"

if [[ -f "${BOOTSTRAP_REPO_DIR}/scripts/lib/deploy_common.sh" ]]; then
  # shellcheck source=scripts/lib/deploy_common.sh
  source "${BOOTSTRAP_REPO_DIR}/scripts/lib/deploy_common.sh"
fi

echo ""
echo "==> [4/5] Deploy de homologação via Ansible (build pode levar alguns minutos)..."
deploy_common::run_ansible_deploy \
  "homolog" \
  "${BOOTSTRAP_REPO_DIR}" \
  "${ANSIBLE_INVENTORY}" \
  "${REPO_URL}" \
  "${BRANCH}" \
  "${DB_PASSWORD}"

echo ""
echo "==> [5/5] Verificando resultado..."
deploy_common::print_deploy_success "homolog" "${SERVER_PORT}" "${APP_DIR}"
