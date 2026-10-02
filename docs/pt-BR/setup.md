# Setup e testes

[English](../en/setup.md) · [Fluxo Docker](docker.md)

Executar somente via Docker não exige Java ou Node no host. Desenvolvimento direto exige JDK 25 (`JAVA_HOME`), Node 22.23.3 (`frontend/.nvmrc`), npm e PostgreSQL 17. Maven Wrapper baixa Maven 3.9.11 no primeiro uso. No Windows utilize `mvnw.cmd` e a sintaxe equivalente de variáveis do shell.

Para DBeaver no host Docker, use host `127.0.0.1`, porta `POSTGRES_HOST_PORT` do `.env` (padrão `5432`), banco `POSTGRES_DB`, usuário `POSTGRES_USER` e senha `POSTGRES_PASSWORD`. Se a porta `5432` estiver ocupada, defina `POSTGRES_HOST_PORT=5433` e use `5433` no DBeaver. Aplique com `docker compose up -d --force-recreate postgres`; os arquivos e as credenciais existentes do banco são preservados.

## Execução direta

Utilize o PostgreSQL do Compose pela porta configurada no host (consulte as [configurações de conexão](docker.md#conexão-pelo-dbeaver)) ou um PostgreSQL local configurado separadamente. Para um banco separado, crie banco/usuário antes. Exporte os valores correspondentes; ao usar Compose, defina `DB_URL` como `jdbc:postgresql://localhost:<POSTGRES_HOST_PORT>/<POSTGRES_DB>` e utilize as credenciais existentes do banco. O `.env` do Compose não configura automaticamente esses processos.

```sh
# PostgreSQL 17 em localhost:5432, com banco e usuário commerce existentes.
export APP_ENV=dev
export DB_URL=jdbc:postgresql://localhost:5432/commerce
export POSTGRES_USER=commerce
export POSTGRES_PASSWORD='your-local-development-password'
export ALLOWED_ORIGINS=http://localhost:4200
export DEV_SEED_ENABLED=true
cd backend
./mvnw spring-boot:run
```

```sh
# Outro terminal, a partir da raiz:
cd frontend
npm ci
npm start
# Caso o backend use outra porta:
API_PROXY_TARGET=http://localhost:8081 npm start
```

`DB_URL` define o nome do banco direto; `POSTGRES_DB` inicializa somente o container. Backend direto usa 8080; a variável padrão Spring `SERVER_PORT` permite mudar. `BACKEND_HOST_PORT` afeta apenas publicação no Compose. As requisições do navegador permanecem relativas; o proxy é usado pelo servidor Node.

## Verificação

```sh
(cd backend && ./mvnw verify)
(cd backend && ./mvnw verify -Pintegration)
(cd frontend && npm ci && npm run build && npm test)
docker compose config --quiet
docker compose up --build -d --wait
curl -f http://localhost:8080/actuator/health/readiness
curl -f http://localhost:4200/api/v1/stores
curl -f http://localhost:8080/api/v1/stores > /tmp/stores-before.json
docker compose restart postgres backend
docker compose up -d --wait
curl -f http://localhost:8080/api/v1/stores > /tmp/stores-after.json
diff /tmp/stores-before.json /tmp/stores-after.json
```

O build Maven comum executa cinco testes MVC: DTO, lista vazia, bloqueios de segurança, CORS explícito e erro sem detalhes internos. O perfil de integração adiciona cinco testes Testcontainers com PostgreSQL real: Flyway, filtro/ordenação via HTTP, resultado vazio, constraints, seed repetível, saúde e OpenAPI local. Se Docker estiver indisponível, a integração falha explicitamente. Dados descartáveis dos testes ficam separados de `.dockerized-postgres`.

Os testes frontend cobrem carregamento → sucesso, lista vazia e erro → nova tentativa → sucesso, usando o serviço tipado real e um backend HTTP de testes. O build de produção verifica templates Angular e TypeScript. jsdom verifica o DOM, mas não layout no navegador ou live reload; abra a aplicação para avaliação visual.

Para testar falha de readiness neste ambiente exclusivamente local, pare PostgreSQL, consulte os dois probes e inicie o banco novamente. Liveness deve permanecer UP; readiness retorna DOWN (503) após o timeout de conexão. Não execute esse teste de interrupção em ambientes compartilhados.

JAR: `backend/target/backend-0.0.1-SNAPSHOT.jar`. Angular: `frontend/dist/commerce/browser`. Live reload observa `src`; Java exige `docker compose up -d --build backend`. Consulte os [resultados registrados](verification.md).
