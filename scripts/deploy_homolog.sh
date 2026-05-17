#!/usr/bin/env bash
# ==============================================================================
# deploy_homolog.sh — Fase F: Deploy semi-automatizado de Homologação
# Uso: sudo bash scripts/deploy_homolog.sh
# ==============================================================================
set -euo pipefail

# ------------------------------------------------------------------------------
# CONFIGURAÇÃO — edite as variáveis abaixo antes de rodar pela primeira vez
# ------------------------------------------------------------------------------
REPO_URL="git@github.com:luccaschneider/despesas-receitas.git"   # ex: https://github.com/usuario/despesas-receitas.git
BRANCH="main"
APP_DIR="/opt/app/homolog"

# Banco de dados — homologação
export POSTGRES_DB="homolog_db"
export POSTGRES_PORT="5432"
export DB_URL="jdbc:postgresql://db:5432/${POSTGRES_DB}"
export DB_USER="postgres"
export DB_PASSWORD="PREENCHA_A_SENHA_DO_BANCO"    # nunca commite a senha real

# Aplicação
export SERVER_PORT="8080"
export SPRING_PROFILES_ACTIVE="homolog"

# E-mail (opcional — mantenha false se não usar)
export APP_EMAIL_ENABLED="false"
export APP_EMAIL_DESTINATARIO=""
export APP_EMAIL_REMETENTE=""
export APP_EMAIL_SMTP_HOST=""
export APP_EMAIL_SMTP_PORT="587"
export APP_EMAIL_SMTP_USERNAME=""
export APP_EMAIL_SMTP_PASSWORD=""
export APP_EMAIL_SMTP_AUTH="true"
export APP_EMAIL_SMTP_STARTTLS="true"
export APP_EMAIL_SMTP_DEBUG="false"
# ------------------------------------------------------------------------------

# --- Validações básicas ---
if [[ "$(id -u)" -ne 0 ]]; then
  echo "[ERRO] Execute como root: sudo bash $0" >&2
  exit 1
fi

if [[ "${REPO_URL}" == "PREENCHA_A_URL_DO_REPOSITORIO" ]]; then
  echo "[ERRO] Defina a variável REPO_URL neste script antes de executar." >&2
  exit 1
fi

if [[ "${DB_PASSWORD}" == "PREENCHA_A_SENHA_DO_BANCO" ]]; then
  echo "[ERRO] Defina a variável DB_PASSWORD neste script antes de executar." >&2
  exit 1
fi

echo "==> [1/4] Navegando para o diretório de homologação: ${APP_DIR}"
mkdir -p "${APP_DIR}"
cd "${APP_DIR}"

echo "==> [2/4] Obtendo código-fonte (branch: ${BRANCH})..."
if [[ ! -d ".git" ]]; then
  echo "    Repositório não encontrado — clonando..."
  git clone --branch "${BRANCH}" "${REPO_URL}" .
else
  echo "    Repositório já existe — atualizando..."
  git fetch origin
  git checkout "${BRANCH}"
  git pull origin "${BRANCH}"
fi

echo "==> [3/4] Variáveis de ambiente configuradas:"
echo "    SERVER_PORT          = ${SERVER_PORT}"
echo "    SPRING_PROFILES_ACTIVE = ${SPRING_PROFILES_ACTIVE}"
echo "    POSTGRES_DB          = ${POSTGRES_DB}"
echo "    DB_URL               = ${DB_URL}"
echo "    DB_USER              = ${DB_USER}"
echo "    APP_EMAIL_ENABLED    = ${APP_EMAIL_ENABLED}"

echo "==> [4/4] Subindo contêineres (--build força rebuild da imagem)..."
docker compose up -d --build

echo ""
echo "========================================================"
echo "  Deploy de HOMOLOGAÇÃO concluído!"
echo "  Acesse: http://<IP_DA_VM>:${SERVER_PORT}"
echo "  Logs  : docker compose -f ${APP_DIR}/docker-compose.yml logs -f app"
echo "========================================================"
