# Deploy com Ansible

Este diretorio contem os playbooks usados pelo painel `/admin/ambientes`, pelos scripts `bootstrap_homolog.sh`, `deploy_homolog.sh` e `deploy_prod.sh`.

## Arquivos principais

- `inventory/hosts.yml`: inventario padrao para deploy **na propria VM** (`ansible_connection: local`). Cada ambiente tem host proprio (`homolog-vm`, `prod-vm`) para nao misturar `group_vars`.
- `inventory/hosts-dev.yml`: inventario para dev local com tunel SSH ate a VM.
- `inventory/group_vars/homolog.yml`: variaveis de homologacao.
- `inventory/group_vars/prod.yml`: variaveis de producao.
- `playbooks/deploy.yml`: clona/atualiza o repositorio, gera `.env` e executa `docker compose up -d --build`.
- `playbooks/stop.yml`: executa `docker compose stop` sem remover volumes.
- `playbooks/status.yml`: consulta `docker compose ps` e emite `ENV_STATUS=running|stopped`.

## Fluxo na VM (apresentacao)

O `bootstrap_homolog.sh`:

1. Instala Git, curl e Ansible
2. Instala Docker Engine + Compose
3. Clona o repositorio em `/opt/despesas-receitas` (playbooks)
4. Executa `ansible-playbook ... deploy.yml -l homolog`

O playbook Ansible faz o clone em `/opt/app/homolog`, gera o `.env` e sobe os containers.

## Inventarios

| Arquivo | Quando usar |
|---------|-------------|
| `hosts.yml` | Bootstrap na VM, scripts `deploy_*.sh` na VM, painel admin rodando **na VM** |
| `hosts-dev.yml` | Painel ou Ansible rodando na **sua maquina** com tunel SSH (`127.0.0.1:2222`) |

Para dev local com o painel admin, configure:

```properties
app.deploy.ansible.inventory=ansible/inventory/hosts-dev.yml
```

Ou via ambiente:

```bash
export APP_DEPLOY_ANSIBLE_INVENTORY=ansible/inventory/hosts-dev.yml
```

## Configuracao obrigatoria

Antes de usar em producao real, ajuste:

- `db_password` (padrao de demo: `despesas_demo_2026`)
- `inventory/hosts-dev.yml` (usuario, porta SSH e chave)

Para segredos reais, prefira Ansible Vault em vez de commitar senhas no repositorio.

## Teste manual na VM

```bash
cd /opt/despesas-receitas   # ou /opt/app/homolog apos o primeiro deploy
sudo ansible-playbook -i ansible/inventory/hosts.yml ansible/playbooks/status.yml -l homolog
sudo ansible-playbook -i ansible/inventory/hosts.yml ansible/playbooks/deploy.yml -l homolog
sudo ansible-playbook -i ansible/inventory/hosts.yml ansible/playbooks/stop.yml -l homolog
```

## Teste manual no dev (tunel SSH)

```bash
ansible-playbook -i ansible/inventory/hosts-dev.yml ansible/playbooks/deploy.yml -l homolog
```
