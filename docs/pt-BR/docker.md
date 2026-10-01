# Docker e configuração

[English](../en/docker.md) · [README](../../README.pt-BR.md)

Utilize Docker Compose v2 com BuildKit. Copie `.env.example` sem substituir configuração existente, edite e execute:

```sh
cp -n .env.example .env
docker compose config --quiet
docker compose up --build
# Ou em segundo plano, aguardando saúde:
docker compose up --build -d --wait
docker compose ps
docker compose logs -f backend
```

Exatamente três serviços compartilham uma bridge interna: frontend, backend e postgres. As portas de frontend, backend e PostgreSQL são configuradas para publicação em loopback. Uma segunda bridge, `web`, conecta apenas as aplicações e permite publicar portas no host: Na validação com Docker Engine 29.5.3, portas de containers conectados exclusivamente a uma rede interna não foram publicadas. PostgreSQL também participa de uma bridge dedicada não interna, `db_access`, que permite publicar sua porta em loopback no Docker Engine 29.5.3. Apenas PostgreSQL participa de `db_access`; `commerce` permanece interna e o backend continua usando `postgres:5432`. O health check do banco expande `$$POSTGRES_USER` e `$$POSTGRES_DB` dentro do container. Backend aguarda o banco; frontend aguarda readiness do backend. A política de reinício é `unless-stopped`. Não há serviços externos necessários em execução.

O backend usa build multi-stage com JDK/Maven Wrapper e execução com JRE e usuário não root. O health check usa Bash `/dev/tcp`, `head` e `grep`, disponíveis na imagem Ubuntu fixada. Não há bind de fontes sobre o JAR. Frontend usa Node não root e `npm ci`. Apenas `frontend/src` é montado como leitura, mantendo dependências Linux na imagem; Angular utiliza polling para live reload. Alterações de dependências, proxy ou configuração Angular exigem rebuild.

```sh
docker compose up -d --build backend # Alterações Java; sem hot reload do backend
docker compose up -d --build frontend # Alterações de dependências/configuração
docker compose restart postgres backend # Reinício sem apagar dados
docker compose down # Remove containers e rede; mantém arquivos do banco
```

## Contrato de ambiente

| Variável | Padrão / finalidade |
| --- | --- |
| `APP_ENV` | Compose `dev`; execução direta `default`. Apenas `dev` habilita OpenAPI local e permite seed. |
| `FRONTEND_HOST_PORT` | `4200`; somente host. |
| `BACKEND_HOST_PORT` | `8080`; somente host. |
| `POSTGRES_HOST_PORT` | `5432`; porta PostgreSQL no host, vinculada a `127.0.0.1`. |
| `POSTGRES_DB` | `commerce`; nome inicial do banco. |
| `POSTGRES_USER` | `commerce`; proprietário do banco local. |
| `POSTGRES_PASSWORD` | Obrigatória; exemplo apenas para desenvolvimento. |
| `ALLOWED_ORIGINS` | Origens explícitas separadas por vírgula; exemplo inclui localhost e 127.0.0.1:4200. |
| `DEV_SEED_ENABLED` | Padrão false; exemplo true; também exige `dev`. |
| `APP_UID` / `APP_GID` | Argumentos de build do backend; padrão `10001`. |
| `DB_URL` | Backend direto: `jdbc:postgresql://localhost:5432/commerce`; Compose monta usando `postgres:5432` e `POSTGRES_DB`. |
| `API_PROXY_TARGET` | Angular direto: `http://localhost:8080`; Compose define `http://backend:8080`. Nunca incluído no código do navegador. |

Mudar portas do host não altera endereços internos. Ajuste `ALLOWED_ORIGINS` quando mudar a origem do navegador. `.env` serve para interpolação do Compose; Maven e npm não o carregam automaticamente. Consulte [execução direta](setup.md).

## Conexão pelo DBeaver

Crie uma conexão PostgreSQL no DBeaver com os valores do `.env` existente:

| Configuração | Valor |
| --- | --- |
| Host | `127.0.0.1` |
| Porta | `POSTGRES_HOST_PORT` (padrão `5432`) |
| Banco | `POSTGRES_DB` |
| Usuário | `POSTGRES_USER` |
| Senha | `POSTGRES_PASSWORD` |

Se a porta `5432` do host já estiver em uso, defina `POSTGRES_HOST_PORT=5433` no `.env` e use a porta `5433` no DBeaver. Aplique o mapeamento com `docker compose up -d --force-recreate postgres`; isso mantém o bind mount e os dados existentes. O backend continua conectando a `postgres:5432` dentro do Docker, independentemente da porta do host. Preserve as credenciais do banco já inicializado; não é necessário resetar os dados.

## Schema e dados de desenvolvimento

Flyway executa migrations na inicialização e Hibernate valida os mapeamentos. O seed exige `APP_ENV=dev` e `DEV_SEED_ENABLED=true`; insere duas lojas fictícias ativas e uma inativa de forma idempotente. Defina false e recrie o backend para impedir novas execuções do seed. Registros existentes permanecem. Para observar um banco vazio sem apagar dados, use um banco novo com seed desabilitado (ou o teste automatizado isolado de banco vazio).

Os dados PostgreSQL 17 ficam em `./.dockerized-postgres:/var/lib/postgresql/data`, ignorados pelo Git. `docker compose down`, inclusive `down -v`, **não** apaga o bind mount. Alterar as variáveis iniciais de banco/usuário/senha não modifica um banco já inicializado. Preserve as credenciais ou altere-as deliberadamente pela administração PostgreSQL.

**Exclusão permanente — reset local opcional. O comando abaixo destrói todos os registros locais. Faça backup antes, se necessário. Não faz parte da verificação.**

```sh
docker compose down
rm -rf -- ./.dockerized-postgres
```

Não são necessários segredos Stripe/JWT. Servidor de desenvolvimento, credenciais locais e conta proprietária do banco não representam configuração de produção.

A [referência de redes Docker](https://docs.docker.com/engine/network/) explica a conexão de aplicações a bridges internas e externas.
