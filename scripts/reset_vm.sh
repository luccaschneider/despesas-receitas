#!/usr/bin/env bash
# ==============================================================================
# reset_vm.sh — Limpa homolog/prod na VM para um novo teste
#
# Uso na VM:
#   sudo bash scripts/reset_vm.sh
#   sudo RESET_PURGE_DIRS=1 bash scripts/reset_vm.sh
#   sudo RESET_PURGE_IMAGES=1 bash scripts/reset_vm.sh
#
# Via curl (mesmo padrão do bootstrap):
#   curl -fsSL https://raw.githubusercontent.com/luccaschneider/despesas-receitas/main/scripts/reset_vm.sh | sudo bash
#
# Variáveis opcionais:
#   RESET_PURGE_DIRS=1    Remove /opt/app/homolog, /opt/app/prod e /opt/despesas-receitas
#   RESET_PURGE_IMAGES=1  Remove imagens Docker homolog-app e prod-app
# ==============================================================================
set -euo pipefail

HOMOLOG_DIR="/opt/app/homolog"
PROD_DIR="/opt/app/prod"
BOOTSTRAP_DIR="/opt/despesas-receitas"

reset_vm::require_root() {
  if [[ "$(id -u)" -ne 0 ]]; then
    echo "[ERRO] Execute como root: sudo bash $0" >&2
    exit 1
  fi
}

reset_vm::truthy() {
  [[ "${1:-}" == "1" || "${1:-}" == "true" || "${1:-}" == "yes" ]]
}

reset_vm::compose_down() {
  local app_dir="$1"
  local label="$2"

  if [[ ! -f "${app_dir}/docker-compose.yml" ]]; then
    echo "    ${label}: sem docker-compose.yml em ${app_dir} — ignorando"
    return 0
  fi

  echo "    ${label}: parando stack e removendo volumes..."
  (
    cd "${app_dir}"
    docker compose down -v --remove-orphans
  ) || true
}

reset_vm::remove_named_containers() {
  local name
  while IFS= read -r name; do
    [[ -z "${name}" ]] && continue
    echo "    Removendo container ${name}..."
    docker rm -f "${name}" >/dev/null 2>&1 || true
  done < <(
    docker ps -a --format '{{.Names}}' 2>/dev/null \
      | grep -E '^(homolog|prod)-|my_postgres$' || true
  )
}

reset_vm::remove_named_volumes() {
  local volume
  for volume in homolog_pgdata prod_pgdata; do
    if docker volume inspect "${volume}" >/dev/null 2>&1; then
      echo "    Removendo volume ${volume}..."
      docker volume rm "${volume}" >/dev/null 2>&1 || true
    fi
  done
}

reset_vm::remove_named_networks() {
  local network
  for network in homolog_default prod_default; do
    if docker network inspect "${network}" >/dev/null 2>&1; then
      echo "    Removendo rede ${network}..."
      docker network rm "${network}" >/dev/null 2>&1 || true
    fi
  done
}

reset_vm::stop_system_postgres() {
  if command -v systemctl >/dev/null 2>&1 && systemctl list-unit-files postgresql.service >/dev/null 2>&1; then
    if systemctl is-active --quiet postgresql; then
      echo "    Parando PostgreSQL do sistema (libera porta 5432)..."
      systemctl stop postgresql
    else
      echo "    PostgreSQL do sistema ja esta parado"
    fi
    return 0
  fi

  echo "    PostgreSQL do sistema nao encontrado — ignorando"
}

reset_vm::purge_directories() {
  local dir
  for dir in "${HOMOLOG_DIR}" "${PROD_DIR}" "${BOOTSTRAP_DIR}"; do
    if [[ -e "${dir}" ]]; then
      echo "    Removendo diretorio ${dir}..."
      rm -rf "${dir}"
    fi
  done
}

reset_vm::purge_images() {
  local image
  while IFS= read -r image; do
    [[ -z "${image}" ]] && continue
    echo "    Removendo imagem ${image}..."
    docker rmi -f "${image}" >/dev/null 2>&1 || true
  done < <(
    docker images --format '{{.Repository}}:{{.Tag}}' 2>/dev/null \
      | grep -E '^(homolog-app|prod-app):' || true
  )
}

reset_vm::print_status() {
  echo ""
  echo "==> Estado atual"
  echo ""
  echo "Containers:"
  docker ps -a --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}' 2>/dev/null || docker ps -a
  echo ""
  echo "Portas (5432 homolog-db, 5433 prod-db, 8081 app homolog, 80 app prod):"
  if command -v ss >/dev/null 2>&1; then
    ss -tlnp | grep -E ':(5432|5433|8081|80)[[:space:]]' || echo "    Nenhuma dessas portas em uso"
  else
    echo "    Comando ss nao disponivel"
  fi
  echo ""
  echo "Diretorios:"
  ls -ld "${HOMOLOG_DIR}" "${PROD_DIR}" "${BOOTSTRAP_DIR}" 2>/dev/null || echo "    Diretorios de deploy ausentes"
}

reset_vm::require_root

echo "========================================================"
echo "  Reset da VM: homolog / prod"
echo "========================================================"

echo ""
echo "==> [1/4] Parando stacks Docker (homolog e prod)..."
reset_vm::compose_down "${HOMOLOG_DIR}" "Homolog"
reset_vm::compose_down "${PROD_DIR}" "Producao"

echo ""
echo "==> [2/4] Removendo containers, volumes e redes residuais..."
reset_vm::remove_named_containers
reset_vm::remove_named_volumes
reset_vm::remove_named_networks

echo ""
echo "==> [3/4] Liberando porta 5432 (PostgreSQL do sistema)..."
reset_vm::stop_system_postgres

if reset_vm::truthy "${RESET_PURGE_DIRS:-}"; then
  echo ""
  echo "==> Opcional: removendo diretorios de deploy..."
  reset_vm::purge_directories
fi

if reset_vm::truthy "${RESET_PURGE_IMAGES:-}"; then
  echo ""
  echo "==> Opcional: removendo imagens Docker da aplicacao..."
  reset_vm::purge_images
fi

echo ""
echo "==> [4/4] Verificacao final"
reset_vm::print_status

echo ""
echo "========================================================"
echo "  Reset concluido"
echo ""
echo "  Proximo passo:"
echo "    curl -fsSL https://raw.githubusercontent.com/luccaschneider/despesas-receitas/main/scripts/bootstrap_homolog.sh | sudo bash"
echo ""
echo "  Variaveis uteis:"
echo "    RESET_PURGE_DIRS=1    apaga clones em /opt/app e /opt/despesas-receitas"
echo "    RESET_PURGE_IMAGES=1  apaga imagens homolog-app / prod-app"
echo "========================================================"
