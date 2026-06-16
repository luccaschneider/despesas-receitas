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
