# Roteiro de Apresentação — VM Zerada → Homolog → Produção

Este documento descreve a **Parte 1** da apresentação: subir o projeto do zero em uma VM Ubuntu limpa, demonstrar homologação e produção com CRUD básico.

---

## Pré-requisitos

| Item | Detalhe |
|------|---------|
| SO da VM | Ubuntu 22.04 ou 24.04 (recomendado) |
| Acesso | SSH à VM + navegador no seu computador |
| Rede | VM com internet (GitHub + Docker Hub) |
| Repositório | Público no GitHub (`luccaschneider/despesas-receitas`, branch `main`) |
| Portas liberadas | `8081` (homolog) e `80` (prod) no firewall/security group |

**Usuário padrão da aplicação** (criado automaticamente pelo Flyway na primeira subida):

| Campo | Valor |
|-------|-------|
| Login | `admin` |
| Senha | `admin123` |
| Perfil | ADMIN (acesso a Ambientes e Configurações) |

A senha do banco usada nos scripts de demo é `despesas_demo_2026` (apenas para apresentação; troque em ambientes reais).

---

## Visão geral do fluxo

```
VM zerada
   │
   ├─► Mostrar que Docker está vazio
   │
   ├─► 1 comando: bootstrap_homolog.sh
   │       ├─ instala Git + Docker
   │       ├─ clona o repositório
   │       └─ sobe homologação (porta 8081)
   │
   ├─► Demo CRUD em homolog
   │
   ├─► deploy_prod.sh
   │       └─ sobe produção (porta 80)
   │
   └─► Demo CRUD em produção
```

---

## Passo a passo (comandos na ordem)

### 0. Conectar na VM

No seu computador:

```bash
ssh usuario@IP_DA_VM
```

Substitua `usuario` e `IP_DA_VM` pelos valores reais.

---

### 1. Mostrar que a VM está “zerada”

Execute e comente o que aparece (listas vazias ou quase vazias):

```bash
docker ps -a
docker images
docker compose version
ls /opt/app 2>/dev/null || echo "/opt/app ainda não existe"
```

**O que dizer:** “Ainda não temos containers, imagens da aplicação nem diretórios de deploy.”

---

### 2. Subir homologação com um único comando

Este é o comando principal da apresentação. Ele baixa o script do GitHub e executa tudo:

```bash
curl -fsSL https://raw.githubusercontent.com/luccaschneider/despesas-receitas/main/scripts/bootstrap_homolog.sh | sudo bash
```

**O que esse comando faz:**

1. Atualiza pacotes e instala Git, curl
2. Instala Docker Engine + Docker Compose (repositório oficial Ubuntu)
3. Clona `https://github.com/luccaschneider/despesas-receitas.git` em `/opt/app/homolog`
4. Gera o arquivo `.env` com as variáveis de homologação
5. Executa `docker compose up -d --build`

> **Tempo estimado:** 5–15 minutos na primeira vez (download de imagens + build Maven dentro do Docker). Vale ensaiar antes.

**Alternativa** (se o repositório ainda não estiver no GitHub ou quiser testar localmente):

```bash
# Na VM, após copiar/clonar o projeto manualmente:
sudo bash scripts/bootstrap_homolog.sh
```

---

### 3. Acompanhar o deploy (opcional, se quiser mostrar logs)

Em outro terminal SSH:

```bash
cd /opt/app/homolog
sudo docker compose ps
sudo docker compose logs -f app
```

Aguarde até o healthcheck ficar saudável. Pressione `Ctrl+C` para sair dos logs.

Verificar saúde via HTTP:

```bash
curl -s http://localhost:8081/actuator/health
```

Resposta esperada: `"status":"UP"`.

---

### 4. Mostrar que homolog está rodando

```bash
docker ps
docker images
ls -la /opt/app/homolog
```

**No navegador** (no seu PC, não na VM):

```
http://IP_DA_VM:8081
```

**Login:**

- Usuário: `admin`
- Senha: `admin123`

---

### 5. Demo CRUD em homologação

Sugestão de roteiro na interface:

1. **Listar** — mostrar lançamentos de exemplo (seed do Flyway)
2. **Criar** — `+ Novo` → receita ou despesa → salvar
3. **Editar** — alterar um lançamento existente
4. **Excluir** — remover o que acabou de criar (com confirmação)
5. **Filtrar** — usar filtros por data/tipo/situação
6. *(Opcional)* **Exportar PDF**

**O que dizer:** “Homologação roda na porta 8081, com perfil Spring `homolog`, banco `homolog_db` e dados isolados de produção.”

---

### 6. Subir produção

Com homolog já no ar, execute:

```bash
sudo bash /opt/app/homolog/scripts/deploy_prod.sh
```

**O que esse comando faz:**

1. Clona/atualiza o repositório em `/opt/app/prod`
2. Gera `.env` de produção (porta `80`, banco `prod_db`, Postgres no host na porta `5433`)
3. Sobe os containers com `docker compose up -d --build`

> Produção usa **porta 80** no host e Postgres exposto em **5433** para não conflitar com homolog (5432).

Acompanhar (opcional):

```bash
cd /opt/app/prod
sudo docker compose ps
curl -s http://localhost/actuator/health
```

---

### 7. Demo CRUD em produção

**No navegador:**

```
http://IP_DA_VM
```

(porta 80 — sem `:8081`)

Login: `admin` / `admin123`

Repita o CRUD básico. **Destaque:** os dados de produção são independentes dos de homolog (bancos e volumes Docker separados).

---

### 8. Comandos úteis para encerrar ou troubleshooting

```bash
# Status dos dois ambientes
sudo docker compose -f /opt/app/homolog/docker-compose.yml ps
sudo docker compose -f /opt/app/prod/docker-compose.yml ps

# Parar homolog (mantém volumes/dados)
cd /opt/app/homolog && sudo docker compose stop

# Parar produção
cd /opt/app/prod && sudo docker compose stop

# Logs
sudo docker compose -f /opt/app/homolog/docker-compose.yml logs -f app
sudo docker compose -f /opt/app/prod/docker-compose.yml logs -f app
```

---

## Resumo dos comandos (cola rápida)

```bash
# 1. VM zerada — mostrar estado vazio
docker ps -a && docker images

# 2. Um comando: dependências + clone + homolog
curl -fsSL https://raw.githubusercontent.com/luccaschneider/despesas-receitas/main/scripts/bootstrap_homolog.sh | sudo bash

# 3. Verificar homolog
curl -s http://localhost:8081/actuator/health
# Browser: http://IP_DA_VM:8081  →  admin / admin123

# 4. Subir produção
sudo bash /opt/app/homolog/scripts/deploy_prod.sh

# 5. Verificar prod
curl -s http://localhost/actuator/health
# Browser: http://IP_DA_VM  →  admin / admin123
```

---

## Estrutura na VM após a apresentação

```
/opt/app/
├── homolog/          # código + docker-compose + .env (porta 8081)
│   └── volumes Docker: banco homolog_db
└── prod/             # código + docker-compose + .env (porta 80)
    └── volumes Docker: banco prod_db
```

---

## Checklist antes do dia da apresentação

- [ ] Repositório público no GitHub com branch `main` atualizada (scripts commitados)
- [ ] Ensaio completo em VM Ubuntu limpa (do `curl` até prod)
- [ ] Portas 8081 e 80 liberadas no firewall
- [ ] Anotar IP da VM e testar acesso pelo navegador
- [ ] Cronômetro: medir tempo do bootstrap (para saber quanto falar durante o build)
- [ ] Ter plano B: se `curl` falhar, clonar manualmente e rodar `sudo bash scripts/bootstrap_homolog.sh`

---

## Outras formas de fazer (referência)

| Abordagem | Quando usar |
|-----------|-------------|
| `curl \| sudo bash` | **Recomendado na apresentação** — um comando, VM sem Git prévio |
| `setup_vm.sh` + `deploy_homolog.sh` | Dois passos, mais controle |
| Painel `/admin/ambientes` (Ansible) | Parte 2 da apresentação; exige app já rodando e Ansible/SSH configurados |

---

## Parte 2 (futuro)

O painel **Admin → Ambientes** (`/admin/ambientes`) permite subir/parar homolog e prod via Ansible. Depende de configuração extra (inventário SSH, senhas no `group_vars`). A Parte 1 acima usa apenas os scripts Bash e não precisa do painel Ansible.
