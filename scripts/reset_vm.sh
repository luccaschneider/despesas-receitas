#!/usr/bin/env bash
# ==============================================================================
# reset_vm.sh — Limpa homolog e prod na VM (estado pronto para novo bootstrap)
#
# Uso na VM:
#   sudo bash scripts/reset_vm.sh
#
# Via curl:
#   curl -fsSL https://raw.githubusercontent.com/luccaschneider/despesas-receitas/main/scripts/reset_vm.sh | sudo bash
#
# Padrao: remove containers, volumes, redes, diretorios de deploy e imagens da app.
#
# Variavel opcional:
#   RESET_QUICK=1   Mantem /opt/app/*, /opt/despesas-receitas e imagens Docker
#                   (so para containers/volumes/redes e libera portas)
# ==============================================================================
set -euo pipefail

HOMOLOG_DIR="/opt/app/homolog"
PROD_DIR="/opt/app/prod"
BOOTSTRAP_DIR="/opt/despesas-receitas"
APP_PORTS=(8081 8082)
DB_PORTS=(5432 5433)

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
    echo "    ${label}: sem docker-compose.yml em ${app_dir}"
    return 0
  fi

  echo "    ${label}: docker compose down (volumes e orfaos)..."
  (
    cd "${app_dir}"
    docker compose down -v --remove-orphans
  ) || true
}

reset_vm::remove_project_containers() {
  local name
  while IFS= read -r name; do
    [[ -z "${name}" ]] && continue
    echo "    Removendo container ${name}..."
    docker rm -f "${name}" >/dev/null 2>&1 || true
  done < <(
    docker ps -a --format '{{.Names}}' 2>/dev/null \
      | grep -E '^(homolog|prod)-' || true
  )

  # Container legado de testes anteriores
  docker rm -f my_postgres >/dev/null 2>&1 || true
}

reset_vm::remove_project_volumes() {
  local volume
  while IFS= read -r volume; do
    [[ -z "${volume}" ]] && continue
    echo "    Removendo volume ${volume}..."
    docker volume rm -f "${volume}" >/dev/null 2>&1 || true
  done < <(
    docker volume ls --format '{{.Name}}' 2>/dev/null \
      | grep -E '^(homolog|prod)_' || true
  )

  for volume in homolog_pgdata prod_pgdata; do
    if docker volume inspect "${volume}" >/dev/null 2>&1; then
      echo "    Removendo volume ${volume}..."
      docker volume rm -f "${volume}" >/dev/null 2>&1 || true
    fi
  done
}

reset_vm::remove_project_networks() {
  local network
  while IFS= read -r network; do
    [[ -z "${network}" ]] && continue
    echo "    Removendo rede ${network}..."
    docker network rm "${network}" >/dev/null 2>&1 || true
  done < <(
    docker network ls --format '{{.Name}}' 2>/dev/null \
      | grep -E '^(homolog|prod)_' || true
  )
}

reset_vm::stop_system_postgres() {
  if command -v systemctl >/dev/null 2>&1 \
      && systemctl list-unit-files postgresql.service >/dev/null 2>&1; then
    if systemctl is-active --quiet postgresql; then
      echo "    Parando PostgreSQL do sistema (porta 5432)..."
      systemctl stop postgresql
    else
      echo "    PostgreSQL do sistema ja esta parado"
    fi
    return 0
  fi

  echo "    PostgreSQL do sistema nao encontrado — ignorando"
}

reset_vm::free_port() {
  local port="$1"

  if ! command -v ss >/dev/null 2>&1; then
    return 0
  fi

  if ! ss -tlnp | grep -qE ":${port}[[:space:]]"; then
    return 0
  fi

  echo "    Porta ${port} ainda em uso — tentando liberar..."
  if command -v fuser >/dev/null 2>&1; then
    fuser -k "${port}/tcp" >/dev/null 2>&1 || true
    sleep 1
  fi
}

reset_vm::free_app_ports() {
  local port
  for port in "${APP_PORTS[@]}" "${DB_PORTS[@]}"; do
    reset_vm::free_port "${port}"
  done
}

reset_vm::purge_directories() {
  local dir
  for dir in "${HOMOLOG_DIR}" "${PROD_DIR}" "${BOOTSTRAP_DIR}"; do
    if [[ -e "${dir}" ]]; then
      echo "    Removendo ${dir}..."
      rm -rf "${dir}"
    fi
  done

  if [[ -d /opt/app ]] && [[ -z "$(ls -A /opt/app 2>/dev/null)" ]]; then
    echo "    Removendo /opt/app vazio..."
    rmdir /opt/app 2>/dev/null || true
  fi
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
  local port_list
  port_list=$(IFS=,; echo "${APP_PORTS[*]},${DB_PORTS[*]}")

  echo ""
  echo "==> Estado atual"
  echo ""
  echo "Containers (homolog/prod):"
  local remaining
  remaining="$(docker ps -a --format '{{.Names}}' 2>/dev/null | grep -E '^(homolog|prod)-' || true)"
  if [[ -n "${remaining}" ]]; then
    echo "${remaining}"
  else
    echo "    Nenhum"
  fi
  echo ""
  echo "Portas (${port_list}):"
  if command -v ss >/dev/null 2>&1; then
    ss -tlnp | grep -E ":($(echo "${APP_PORTS[*]} ${DB_PORTS[*]}" | tr ' ' '|'))[[:space:]]" \
      || echo "    Nenhuma em uso"
  else
    echo "    Comando ss nao disponivel"
  fi
  echo ""
  echo "Diretorios de deploy:"
  ls -ld "${HOMOLOG_DIR}" "${PROD_DIR}" "${BOOTSTRAP_DIR}" 2>/dev/null \
    || echo "    Ausentes (VM limpa para novo bootstrap)"
  echo ""
  echo "Imagens da aplicacao:"
  docker images --format 'table {{.Repository}}:{{.Tag}}' 2>/dev/null \
    | grep -E '^(REPOSITORY|homolog-app|prod-app)' || echo "    Nenhuma"
}

reset_vm::require_root

QUICK_MODE=false
if reset_vm::truthy "${RESET_QUICK:-}"; then
  QUICK_MODE=true
fi

echo "========================================================"
echo "  Reset da VM: homolog + prod"
if [[ "${QUICK_MODE}" == true ]]; then
  echo "  Modo: RESET_QUICK (mantem diretorios e imagens)"
else
  echo "  Modo: limpeza completa"
fi
echo "========================================================"

echo ""
echo "==> [1/5] Parando stacks Docker..."
reset_vm::compose_down "${HOMOLOG_DIR}" "Homolog"
reset_vm::compose_down "${PROD_DIR}" "Producao"

echo ""
echo "==> [2/5] Removendo containers, volumes e redes..."
reset_vm::remove_project_containers
reset_vm::remove_project_volumes
reset_vm::remove_project_networks

echo ""
echo "==> [3/5] Liberando portas e PostgreSQL do sistema..."
reset_vm::stop_system_postgres
reset_vm::free_app_ports

if [[ "${QUICK_MODE}" == false ]]; then
  echo ""
  echo "==> [4/5] Removendo diretorios de deploy..."
  reset_vm::purge_directories

  echo ""
  echo "==> [5/5] Removendo imagens Docker da aplicacao..."
  reset_vm::purge_images
else
  echo ""
  echo "==> [4/5] Diretorios e imagens preservados (RESET_QUICK=1)"
  echo "==> [5/5] Pulando remocao de diretorios e imagens"
fi

echo ""
reset_vm::print_status

echo ""
echo "========================================================"
echo "  Reset concluido"
echo ""
echo "  Proximo passo:"
echo "    curl -fsSL https://raw.githubusercontent.com/luccaschneider/despesas-receitas/main/scripts/bootstrap_homolog.sh | sudo bash"
echo ""
echo "  Modo rapido (so containers, sem apagar clones):"
echo "    RESET_QUICK=1 sudo bash scripts/reset_vm.sh"
echo "========================================================"
