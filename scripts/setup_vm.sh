#!/usr/bin/env bash
set -euo pipefail

# --- Validação: deve rodar como root ---
if [[ "$(id -u)" -ne 0 ]]; then
  echo "[ERRO] Este script precisa ser executado como root. Use: sudo bash $0" >&2
  exit 1
fi

echo "==> [1/4] Atualizando índice de pacotes..."
apt-get update -y
apt-get upgrade -y

echo "==> [2/4] Instalando dependências e Git..."
apt-get install -y \
  git \
  ca-certificates \
  curl \
  gnupg \
  lsb-release

echo "==> [3/4] Instalando Docker Engine e docker-compose-plugin..."

# Configura repositório oficial do Docker
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

# Habilita e inicia o serviço Docker
if command -v systemctl &> /dev/null; then
  systemctl enable docker
  systemctl start docker
  echo "    Docker iniciado via systemctl."
else
  echo "    [AVISO] systemctl não encontrado; inicie o Docker manualmente."
fi

echo "    Docker version: $(docker --version)"
echo "    Docker Compose version: $(docker compose version)"

echo "==> [4/4] Criando estrutura de diretórios da aplicação..."
mkdir -p /opt/app/homolog
mkdir -p /opt/app/prod
echo "    Diretórios criados: /opt/app/homolog  /opt/app/prod"

echo ""
echo "========================================================"
echo "  Setup concluído com sucesso!"
echo "  Próximos passos:"
echo "    - Tudo em 1 comando (recomendado):"
echo "        curl -fsSL https://raw.githubusercontent.com/luccaschneider/despesas-receitas/main/scripts/bootstrap_homolog.sh | sudo bash"
echo "    - Ou manualmente:"
echo "        sudo bash scripts/deploy_homolog.sh"
echo "        sudo bash scripts/deploy_prod.sh"
echo "========================================================"
