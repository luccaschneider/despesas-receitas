# Roteiro de Apresentação

---

## Referência

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

### Subir produção (primeira vez)

```bash
sudo bash /opt/app/homolog/scripts/deploy_prod.sh
```

---



1. **Visual** — ex.: alterar um texto em `src/main/resources/templates/lancamentos.html` (label no header ou título). 
2. **Migration** — ex.: `src/main/resources/db/migration/V6__create_tipo.sql`:

```sql
CREATE TABLE tipo (
    id   BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL
);
```

3. **QA** — adicione import nao usado em `AdminController.java` 

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
```

---
