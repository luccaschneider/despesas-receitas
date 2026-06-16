#!/usr/bin/env bash
# Funções compartilhadas pelos scripts de deploy (homolog, prod, bootstrap).

deploy_common::require_root() {
  if [[ "$(id -u)" -ne 0 ]]; then
    echo "[ERRO] Execute como root: sudo bash $0" >&2
    exit 1
  fi
}

deploy_common::default_repo_url() {
  echo "${REPO_URL:-https://github.com/luccaschneider/despesas-receitas.git}"
}

deploy_common::default_branch() {
  echo "${BRANCH:-main}"
}

deploy_common::default_db_password() {
  echo "${DB_PASSWORD:-despesas_demo_2026}"
}

deploy_common::validate_repo_url() {
  local repo_url="$1"
  if [[ "${repo_url}" == "PREENCHA_A_URL_DO_REPOSITORIO" || -z "${repo_url}" ]]; then
    echo "[ERRO] Defina REPO_URL antes de executar." >&2
    exit 1
  fi
}

deploy_common::write_env_file() {
  local app_dir="$1"
  cat > "${app_dir}/.env" <<EOF
POSTGRES_DB=${POSTGRES_DB}
POSTGRES_PORT=${POSTGRES_PORT}
DB_URL=${DB_URL}
DB_USER=${DB_USER}
DB_PASSWORD=${DB_PASSWORD}

SERVER_PORT=${SERVER_PORT}
SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

APP_EMAIL_ENABLED=${APP_EMAIL_ENABLED}
APP_EMAIL_DESTINATARIO=${APP_EMAIL_DESTINATARIO}
APP_EMAIL_REMETENTE=${APP_EMAIL_REMETENTE}
APP_EMAIL_SMTP_HOST=${APP_EMAIL_SMTP_HOST}
APP_EMAIL_SMTP_PORT=${APP_EMAIL_SMTP_PORT}
APP_EMAIL_SMTP_USERNAME=${APP_EMAIL_SMTP_USERNAME}
APP_EMAIL_SMTP_PASSWORD=${APP_EMAIL_SMTP_PASSWORD}
APP_EMAIL_SMTP_AUTH=${APP_EMAIL_SMTP_AUTH}
APP_EMAIL_SMTP_STARTTLS=${APP_EMAIL_SMTP_STARTTLS}
APP_EMAIL_SMTP_DEBUG=${APP_EMAIL_SMTP_DEBUG}
EOF
  chmod 600 "${app_dir}/.env"
}

deploy_common::clone_or_update_repo() {
  local repo_url="$1"
  local branch="$2"
  local app_dir="$3"

  mkdir -p "${app_dir}"
  cd "${app_dir}"

  if [[ ! -d ".git" ]]; then
    echo "    Repositório não encontrado — clonando..."
    git clone --branch "${branch}" "${repo_url}" .
  else
    echo "    Repositório já existe — atualizando..."
    git fetch origin
    git checkout "${branch}"
    git pull origin "${branch}"
  fi
}

deploy_common::print_email_defaults() {
  export APP_EMAIL_ENABLED="${APP_EMAIL_ENABLED:-false}"
  export APP_EMAIL_DESTINATARIO="${APP_EMAIL_DESTINATARIO:-}"
  export APP_EMAIL_REMETENTE="${APP_EMAIL_REMETENTE:-}"
  export APP_EMAIL_SMTP_HOST="${APP_EMAIL_SMTP_HOST:-}"
  export APP_EMAIL_SMTP_PORT="${APP_EMAIL_SMTP_PORT:-587}"
  export APP_EMAIL_SMTP_USERNAME="${APP_EMAIL_SMTP_USERNAME:-}"
  export APP_EMAIL_SMTP_PASSWORD="${APP_EMAIL_SMTP_PASSWORD:-}"
  export APP_EMAIL_SMTP_AUTH="${APP_EMAIL_SMTP_AUTH:-true}"
  export APP_EMAIL_SMTP_STARTTLS="${APP_EMAIL_SMTP_STARTTLS:-true}"
  export APP_EMAIL_SMTP_DEBUG="${APP_EMAIL_SMTP_DEBUG:-false}"
}

deploy_common::default_ansible_inventory() {
  echo "${ANSIBLE_INVENTORY:-ansible/inventory/hosts.yml}"
}

deploy_common::default_bootstrap_repo_dir() {
  echo "${BOOTSTRAP_REPO_DIR:-/opt/despesas-receitas}"
}

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

deploy_common::resolve_ansible_root() {
  local script_dir="${1:-}"
  local candidates=()

  if [[ -n "${script_dir}" && -f "${script_dir}/../ansible/playbooks/deploy.yml" ]]; then
    candidates+=("$(cd "${script_dir}/.." && pwd)")
  fi

  candidates+=("$(deploy_common::default_bootstrap_repo_dir)")
  candidates+=("/opt/app/homolog")

  local candidate
  for candidate in "${candidates[@]}"; do
    if [[ -f "${candidate}/ansible/playbooks/deploy.yml" ]]; then
      echo "${candidate}"
      return 0
    fi
  done

  echo "[ERRO] Nao foi possivel localizar ansible/playbooks/deploy.yml." >&2
  echo "       Execute o bootstrap ou clone o repositorio antes do deploy." >&2
  exit 1
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
  local server_port="${7:-}"

  if [[ ! -f "${ansible_root}/${inventory}" ]]; then
    echo "[ERRO] Inventario Ansible nao encontrado: ${ansible_root}/${inventory}" >&2
    exit 1
  fi

  if ! command -v ansible-playbook &> /dev/null; then
    echo "[ERRO] ansible-playbook nao encontrado. Execute o bootstrap ou instale o Ansible." >&2
    exit 1
  fi

  local extra_vars=(
    -e "repo_url=${repo_url}"
    -e "branch=${branch}"
    -e "db_password=${db_password}"
  )
  if [[ -n "${server_port}" ]]; then
    extra_vars+=(-e "server_port=${server_port}")
  fi

  echo "    Ansible root  = ${ansible_root}"
  echo "    Inventario    = ${inventory}"
  echo "    Ambiente      = ${environment}"
  echo "    Repositorio   = ${repo_url} (${branch})"
  if [[ -n "${server_port}" ]]; then
    echo "    SERVER_PORT   = ${server_port}"
  fi

  (
    cd "${ansible_root}"
    ansible-playbook \
      -i "${inventory}" \
      ansible/playbooks/deploy.yml \
      -l "${environment}" \
      "${extra_vars[@]}"
  )
}

deploy_common::print_deploy_success() {
  local environment="$1"
  local server_port="$2"
  local app_dir="$3"

  local vm_ip
  vm_ip="$(hostname -I 2>/dev/null | awk '{print $1}')"
  vm_ip="${vm_ip:-<IP_DA_VM>}"

  local url="http://${vm_ip}"
  if [[ "${server_port}" != "80" ]]; then
    url="${url}:${server_port}"
  fi

  local title
  if [[ "${environment}" == "prod" ]]; then
    title="Producao pronta!"
  else
    title="Homologacao pronta!"
  fi

  echo ""
  echo "========================================================"
  echo "  ${title}"
  echo "  URL   : ${url}"
  echo "  Login : admin"
  echo "  Senha : admin123"
  echo "  Logs  : docker compose -f ${app_dir}/docker-compose.yml logs -f app"
  if [[ "${environment}" == "homolog" ]]; then
    echo ""
    echo "  Proximo passo (producao):"
    echo "    sudo bash ${app_dir}/scripts/deploy_prod.sh"
  fi
  echo "========================================================"
}
