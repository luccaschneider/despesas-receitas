# Roteiro de Apresentação

---

## Referência

| Item | Valor |
|------|--------|
| Login app | `admin` / `admin123` |
| Senha banco (demo) | `despesas_demo_2026` |
| Homolog | `http://IP_DA_VM:8081` — banco `homolog_db`, Postgres host `5432` |
| Produção | `http://IP_DA_VM:8082` — banco `prod_db`, Postgres host `5433` |
| Paths na VM | `/opt/app/homolog`, `/opt/app/prod`, `/opt/despesas-receitas` |

**Deploy para atualizar código:** `deploy_homolog.sh` e `deploy_prod.sh` fazem `git pull` no GitHub, regeneram `.env`, `docker compose up --build` e o Flyway roda na subida da app.

---

## Parte 1 — VM zerada → homolog → prod

### Reset (entre ensaios)

```bash
curl -fsSL https://raw.githubusercontent.com/luccaschneider/despesas-receitas/main/scripts/reset_vm.sh | sudo bash
```

Limpa containers, volumes, redes, `/opt/app/*`, `/opt/despesas-receitas` e imagens (app + postgres).

### Mostrar VM vazia

```bash
sudo docker ps -a
sudo docker images
```

### Subir homolog (primeira vez)

```bash
curl -fsSL https://raw.githubusercontent.com/luccaschneider/despesas-receitas/main/scripts/bootstrap_homolog.sh | sudo bash
```

O script so exibe a URL apos `/actuator/health` retornar `UP` (ate 120s).

```bash
curl -s http://localhost:8081/actuator/health
```


### Subir produção (primeira vez)

```bash
sudo bash /opt/app/homolog/scripts/deploy_prod.sh
```

```bash
curl -s http://localhost:8082/actuator/health
```

---

## Parte 2 — Código, CI e correção (no seu PC + GitHub)

### O que preparar no código (antes ou ao vivo)

1. **Visual** — ex.: alterar um texto em `src/main/resources/templates/lancamentos.html` (label no header ou título).
2. **Migration** — ex.: `src/main/resources/db/migration/V6__create_tipo.sql`:

```sql
CREATE TABLE tipo (
    id   BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL
);
```

3. **Quebrar QA** — introduzir violação de Checkstyle em algum `.java` (linha longa, import não usado, etc.).

### Fluxo Git / Actions

```bash
# Commit 1: feature + erro de checkstyle
git add .
git commit -m "feat: label demo e tabela tipo"
git push origin main
```

Abrir **GitHub → Actions** e mostrar falha no job **QA (Checkstyle)** (testes podem passar; checkstyle falha).

```bash
# Commit 2: corrigir só o checkstyle
git add .
git commit -m "fix: checkstyle"
git push origin main
```

Mostrar Actions **verde** (testes + checkstyle + docker build).

> Ordem no CI: `mvn clean test` → `mvn checkstyle:check` → `docker build`.

---

## Parte 3 — Atualizar homolog (código novo na VM)

**Pré-requisito:** commit corrigido já no `main` do GitHub.

```bash
sudo bash /opt/app/homolog/scripts/deploy_homolog.sh
```

### Mostrar tabela nova no Postgres (homolog)

```bash
sudo docker compose -f /opt/app/homolog/docker-compose.yml exec db \
  psql -U postgres -d homolog_db -c "\dt"

sudo docker compose -f /opt/app/homolog/docker-compose.yml exec db \
  psql -U postgres -d homolog_db -c "\d tipo"
```

---

## Parte 4 — Atualizar produção

```bash
sudo bash /opt/app/homolog/scripts/deploy_prod.sh
```

### Postgres (prod)

```bash
sudo docker compose -f /opt/app/prod/docker-compose.yml exec db \
  psql -U postgres -d prod_db -c "\dt"

sudo docker compose -f /opt/app/prod/docker-compose.yml exec db \
  psql -U postgres -d prod_db -c "\d tipo"
```

---

## Comandos auxiliares

### Status e logs

```bash
sudo docker compose -f /opt/app/homolog/docker-compose.yml ps
sudo docker compose -f /opt/app/prod/docker-compose.yml ps
sudo docker compose -f /opt/app/homolog/docker-compose.yml logs -f app
sudo docker compose -f /opt/app/prod/docker-compose.yml logs -f app
```

### Parar sem apagar dados

```bash
cd /opt/app/homolog && sudo docker compose stop
cd /opt/app/prod && sudo docker compose stop
```

### Portas em uso

```bash
sudo ss -tlnp | grep -E ':8081|:8082|:5432|:5433'
```

### Postgres do sistema (conflito na 5432)

```bash
sudo systemctl stop postgresql
```

### Reset rápido (mantém clones, só containers)

```bash
curl -fsSL https://raw.githubusercontent.com/luccaschneider/despesas-receitas/main/scripts/reset_vm.sh | sudo RESET_QUICK=1 bash
```

### Remover imagem postgres manualmente

```bash
sudo docker rmi -f postgres:16-alpine
```

### Conferir `.env` de prod (porta)

```bash
grep SERVER_PORT /opt/app/prod/.env
# Esperado: SERVER_PORT=8082
```

---

## Cola rápida (ordem completa)

```bash
# --- Infra ---
curl -fsSL .../reset_vm.sh | sudo bash
curl -fsSL .../bootstrap_homolog.sh | sudo bash
sudo bash /opt/app/homolog/scripts/deploy_prod.sh

# --- Após push no GitHub (CI ok) ---
sudo bash /opt/app/homolog/scripts/deploy_homolog.sh
sudo bash /opt/app/homolog/scripts/deploy_prod.sh
```

---

## Checklist antes do dia

- [ ] `main` no GitHub com scripts e inventário Ansible corrigidos
- [ ] Portas 8081 e 8082 liberadas no firewall
- [ ] Ensaio: reset → bootstrap → prod → push → deploy homolog → deploy prod
- [ ] IP da VM anotado; browser testado
- [ ] Commits da Parte 2 preparados (ou roteiro do que editar ao vivo)
- [ ] Não usar painel **Ambientes** na VM (roda dentro do Docker; use os scripts)

---

## Estrutura na VM

```
/opt/app/homolog/   → homolog (8081, homolog_db)
/opt/app/prod/      → prod (8082, prod_db)
/opt/despesas-receitas/ → playbooks Ansible (bootstrap)
```

| Script | Quando usar |
|--------|-------------|
| `bootstrap_homolog.sh` | VM zerada → instala deps + homolog |
| `deploy_homolog.sh` | Atualizar homolog (git + rebuild) |
| `deploy_prod.sh` | Subir ou atualizar prod |
| `reset_vm.sh` | Limpar tudo entre ensaios |
