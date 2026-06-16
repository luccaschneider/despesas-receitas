# Deploy com Ansible

Este diretorio contem os playbooks usados pelo painel `/admin/ambientes`.

## Arquivos principais

- `inventory/hosts.yml`: hosts dos ambientes `homolog` e `prod`.
- `group_vars/homolog.yml`: variaveis de homologacao.
- `group_vars/prod.yml`: variaveis de producao.
- `playbooks/deploy.yml`: clona/atualiza o repositorio, gera `.env` e executa `docker compose up -d --build`.
- `playbooks/stop.yml`: executa `docker compose stop` sem remover volumes.
- `playbooks/status.yml`: consulta `docker compose ps` e emite `ENV_STATUS=running|stopped`.

## Configuracao obrigatoria

Antes de usar em producao real, ajuste:

- `db_password` (padrao de demo: `despesas_demo_2026`)
- hosts SSH no `inventory/hosts.yml`, caso o painel nao rode na propria VM

Para apresentacao em VM zerada, prefira `scripts/bootstrap_homolog.sh` (ver `docs/APRESENTACAO.md`).

Para segredos reais, prefira Ansible Vault em vez de commitar senhas no repositorio.

## Teste manual

```bash
ansible-playbook -i ansible/inventory/hosts.yml ansible/playbooks/status.yml -l homolog
ansible-playbook -i ansible/inventory/hosts.yml ansible/playbooks/deploy.yml -l homolog
ansible-playbook -i ansible/inventory/hosts.yml ansible/playbooks/stop.yml -l homolog
```
