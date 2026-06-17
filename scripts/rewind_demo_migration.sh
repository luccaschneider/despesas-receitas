#!/usr/bin/env bash
# ==============================================================================
# rewind_demo_migration.sh — Remove tabela "tipo" e registro Flyway V6 (homolog/prod)
#
# Use antes de reapresentar a Parte 2 (criar V6 ao vivo de novo).
# Nao apaga lancamentos nem outros dados — so desfaz a migration de demo.
#
# Uso na VM:
#   sudo bash scripts/rewind_demo_migration.sh
#   sudo bash scripts/rewind_demo_migration.sh homolog   # so homolog
#   sudo bash scripts/rewind_demo_migration.sh prod      # so prod
# ==============================================================================
set -euo pipefail

HOMOLOG_DIR="/opt/app/homolog"
PROD_DIR="/opt/app/prod"

rewind_demo::require_root() {
  [[ "$(id -u)" -eq 0 ]] || { echo "[ERRO] Use sudo." >&2; exit 1; }
}

rewind_demo::rewind_database() {
  local label="$1"
  local compose_dir="$2"
  local db_name="$3"

  if [[ ! -f "${compose_dir}/docker-compose.yml" ]]; then
    echo "    ${label}: ${compose_dir} nao existe — ignorando"
    return 0
  fi

  if ! docker compose -f "${compose_dir}/docker-compose.yml" ps --status running --services 2>/dev/null | grep -qx db; then
    echo "    ${label}: container db nao esta rodando — ignorando"
    return 0
  fi

  echo "    ${label}: removendo tabela tipo e registro Flyway V6 em ${db_name}..."
  docker compose -f "${compose_dir}/docker-compose.yml" exec -T db \
    psql -U postgres -d "${db_name}" -v ON_ERROR_STOP=1 <<'SQL'
DROP TABLE IF EXISTS tipo CASCADE;
DELETE FROM flyway_schema_history WHERE version = '6';
SQL
}

rewind_demo::require_root

TARGET="${1:-all}"

echo "========================================================"
echo "  Reverter migration de demo (V6 / tabela tipo)"
echo "========================================================"

case "${TARGET}" in
  homolog)
    rewind_demo::rewind_database "Homolog" "${HOMOLOG_DIR}" "homolog_db"
    ;;
  prod)
    rewind_demo::rewind_database "Producao" "${PROD_DIR}" "prod_db"
    ;;
  all|*)
    rewind_demo::rewind_database "Homolog" "${HOMOLOG_DIR}" "homolog_db"
    rewind_demo::rewind_database "Producao" "${PROD_DIR}" "prod_db"
    ;;
esac

echo ""
echo "========================================================"
echo "  Pronto. Proximos passos:"
echo "  1. Remova V6__create_tipo.sql do repositorio (se ainda existir) e de push"
echo "  2. Na apresentacao, crie V6 de novo e faca deploy"
echo ""
echo "  Conferir (homolog):"
echo "    sudo docker compose -f ${HOMOLOG_DIR}/docker-compose.yml exec db \\"
echo "      psql -U postgres -d homolog_db -c \"\\dt tipo\""
echo "========================================================"
