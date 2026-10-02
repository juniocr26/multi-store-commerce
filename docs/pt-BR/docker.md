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
| `POSTGRES_DATA_SOURCE` | `./.dockerized-postgres` preserva dados; `postgres_data` seleciona volume nomeado do projeto para clones novos. |
| `MAVEN_CACHE_ID` | `commerce-maven`; namespace opcional de cache BuildKit. |
| `POSTGRES_DB` | `commerce`; nome inicial do banco. |
| `POSTGRES_USER` | `commerce`; proprietário do banco local. |
| `POSTGRES_PASSWORD` | Obrigatória; `commerce-local-only` é público e apenas para desenvolvimento. |
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
| Banco | `POSTGRES_DATA_SOURCE` | `./.dockerized-postgres` preserva dados; `postgres_data` seleciona volume nomeado do projeto para clones novos. |
| `MAVEN_CACHE_ID` | `commerce-maven`; namespace opcional de cache BuildKit. |
| `POSTGRES_DB` |
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

## Builds reproduzíveis e verificação isolada

Esse fluxo exige apenas Docker com Compose v2+ e BuildKit. Java, Maven, Node e
npm ficam nas imagens. No Windows, use containers Linux e, se `.env` ainda não
existir, `Copy-Item .env.example .env`. Os exemplos de shell usam sintaxe POSIX
(Git Bash/WSL no Windows). Preserve credenciais existentes; a senha de exemplo é
pública e exclusiva para desenvolvimento local.

O Maven Wrapper 3.3.4 troca o ZIP por tar.gz quando `unzip` não está disponível,
mas continua validando com o SHA-256 do ZIP. O estágio JDK agora instala `unzip`
para manter o download consistente com o checksum configurado, que não mudou:
`0d7125e8c91097b36edb990ea5934e6c68b4440eef4ea96510a0f6815e7eeadb`.
Esse valor foi calculado após comparar o ZIP com o
[SHA-512 oficial do Maven Central](https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.zip.sha512).
Não remova a validação. Os atributos LF e a normalização no build protegem o
wrapper de CRLF; o Dockerfile também define a permissão de execução.

O cache BuildKit do Maven é opcional. `MAVEN_CACHE_ID` define seu namespace;
quando vazio, wrapper e dependências são baixados novamente. `--no-cache` não
esvazia mounts de cache: use um ID novo para validar builds frios. O frontend
instala com `npm ci` e o lockfile versionado dentro da imagem. Não existe volume
node_modules sujeito a ficar vazio ou desatualizado. Apenas `src` é montado;
node_modules e resultados compilados do host são excluídos do build. Após mudar
package.json **e** package-lock.json, proxy ou configuração Angular, faça rebuild.
Manifestos incompatíveis com o lockfile falham intencionalmente. Remova de
overrides locais mounts antigos sobre `/app` ou `/app/node_modules`, pois
ocultariam as dependências da imagem.

`POSTGRES_DATA_SOURCE` mantém `./.dockerized-postgres` como padrão para preservar
bancos existentes. Em um clone **novo**, defina `POSTGRES_DATA_SOURCE=postgres_data`
no `.env` para usar o volume gerenciado `<projeto>_postgres_data`, criado
automaticamente e sem depender das permissões do filesystem do host. Trocar a
origem seleciona outro banco; não migra dados. Para bancos existentes mantenha a
origem atual, ou faça backup/restauração com ferramentas PostgreSQL antes de trocar.
Preserve as credenciais usadas na inicialização.

```sh
# Ciclo normal, preservando dados
docker compose up -d --build --wait --wait-timeout 180
docker compose stop
docker compose up -d --wait
docker compose up -d --build --wait
docker compose down
# Verificação HTTP, portas padrão
curl --fail http://localhost:8080/actuator/health/readiness
curl --fail http://localhost:8080/api/v1/stores
curl --fail http://localhost:4200/stores
curl --fail http://localhost:4200/api/v1/stores
# Build e testes sem ferramentas no host
docker compose run --rm --no-deps frontend sh -c 'npm run build && npm test'
docker build --target build -t commerce-backend-check ./backend
docker run --rm commerce-backend-check ./mvnw -B -ntp verify
```

Para validar isoladamente, copie/clone os arquivos versionados para um diretório
temporário. Copie `.env.example` para `test.env` ali e defina
`POSTGRES_DATA_SOURCE=postgres_data`, portas livres e um `MAVEN_CACHE_ID` único.
Use o mesmo nome explícito de projeto e arquivo em **todos** os comandos:

```sh
docker compose -p commerce-check --env-file test.env config --quiet
docker compose -p commerce-check --env-file test.env build --no-cache
docker compose -p commerce-check --env-file test.env up -d --wait --wait-timeout 180
docker compose -p commerce-check --env-file test.env ps
docker compose -p commerce-check --env-file test.env restart
docker compose -p commerce-check --env-file test.env up -d --build --wait
docker compose -p commerce-check --env-file test.env down
```

**Reset destrutivo restrito ao projeto:** para volume nomeado, confira primeiro
`docker compose config --volumes` e `docker volume inspect <projeto>_postgres_data`.
`docker compose down --volumes` apaga o volume nomeado desse projeto e todos os
registros; a próxima inicialização recria PostgreSQL e executa Flyway. Para bind,
isso não apaga dados: o procedimento `rm -rf ./.dockerized-postgres` acima continua
sendo destrutivo. Faça backup antes. Nunca execute prune global.

Os digests das imagens são índices com Linux ARM64 e AMD64, sem plataforma
forçada. A inspeção dos manifestos não equivale a executar a aplicação em outra
arquitetura ou em hosts Windows/Linux. Veja o registro atualizado de verificação.
