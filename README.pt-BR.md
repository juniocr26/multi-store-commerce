# multi-store-commerce

[English](README.md)

Projeto de portfólio full stack para uma rede fictícia de padarias. **Implementado: a base executável de listagem de lojas.** Um monólito modular Spring Boot, uma aplicação Angular e PostgreSQL. As lojas pertencem à mesma rede; isolamento entre empresas independentes está fora do escopo.

O backend executa migrations Flyway e expõe `GET /api/v1/stores`: lojas ativas ordenadas pelo slug único, DTOs explícitos `{id, slug, name}` e `[]` quando não há resultados. O Angular apresenta carregamento, lista vazia, sucesso, erro e nova tentativa. A segurança bloqueia rotas não autorizadas. Inclui health checks, OpenAPI local, seed fictício opcional e testes automatizados.

## Iniciar

Instale Docker com Compose v2 e execute na raiz:

```sh
cp -n .env.example .env
# Edite .env: as credenciais de exemplo são apenas para desenvolvimento local.
docker compose up --build
```

Não sobrescreva um `.env` existente. O primeiro build baixa dependências. Os serviços ficam prontos na ordem PostgreSQL, backend e frontend.

- Aplicação: http://localhost:4200/stores
- API: http://localhost:8080/api/v1/stores
- Swagger UI (dev): http://localhost:8080/swagger-ui/index.html
- OpenAPI (dev): http://localhost:8080/v3/api-docs
- Probes: http://localhost:8080/actuator/health/liveness e http://localhost:8080/actuator/health/readiness

As portas do host são configuráveis; as portas internas permanecem 4200, 8080 e 5432. PostgreSQL é configurado em `127.0.0.1:${POSTGRES_HOST_PORT:-5432}` para [acesso pelo DBeaver](docs/pt-BR/docker.md#conexão-pelo-dbeaver).

## Build e testes

Com JDK 21 e Node.js 22.23.3 instalados:

```sh
(cd backend && ./mvnw verify)
(cd backend && ./mvnw verify -Pintegration) # Requer Docker; PostgreSQL real
(cd frontend && npm ci && npm run build && npm test)
docker compose config --quiet
```

Consulte [setup e testes](docs/pt-BR/setup.md), [Docker e configuração](docs/pt-BR/docker.md), [arquitetura](docs/pt-BR/architecture.md), [fontes das versões](docs/pt-BR/versions.md) e [resultados da verificação](docs/pt-BR/verification.md).

## Stack

| Componente | Version |
| --- | --- |
| Java / Eclipse Temurin | 21 LTS / 21.0.12.1+1 |
| Spring Boot | 3.5.16 |
| Maven / Wrapper | 3.9.11 / 3.3.4 |
| springdoc OpenAPI | 2.8.17 |
| Angular / CLI & build | 21.2.25 / 21.2.24 |
| Node.js | 22.23.3 LTS |
| TypeScript / RxJS | 5.9.3 / 7.8.2 |
| PostgreSQL | 17.11 (Debian Bookworm) |

O Spring Boot gerencia Hibernate, JDBC, Flyway (incluindo suporte PostgreSQL), JUnit, Mockito e Testcontainers. As dependências são fixadas pelo POM pai e lockfile npm; as imagens base das aplicações e do PostgreSQL usam digests imutáveis.

## Organização

```text
backend/             Maven Wrapper, Spring, migrations, testes, Dockerfile
frontend/            Angular standalone, testes, lock npm, Dockerfile
docs/en/             Guias em inglês e roadmap preservado
docs/pt-BR/          Guias em português e roadmap preservado
docs/adr/            Decisões arquiteturais aceitas
docker-compose.yml   Três serviços de desenvolvimento
.env.example         Configuração local documentada
```

`infrastructure/` será criado quando houver configuração de apoio com responsabilidade concreta. A infraestrutura atual está no Compose e nos Dockerfiles das aplicações.

## Planejamento e limitações

Identidade/acesso, autorização por loja, clientes, catálogo, estoque, carrinho, checkout, pedidos, cartões Stripe de teste/webhooks, entrega, notificações, relatórios e administração continuam **planejados**. Não existem endpoints fictícios nem usuários falsos. RabbitMQ, Redis e WebSockets são candidatos futuros, sem dependências ou serviços. Consulte o [roadmap de domínio preservado](docs/pt-BR/roadmap.md).

Esta etapa não inclui API de escrita, paginação, autenticação, servidor frontend de produção ou alegação de prontidão para produção. O frontend usa servidor de desenvolvimento; alterações no backend exigem rebuild. Os dados permanecem em `.dockerized-postgres` após remover containers. Desativar o seed não remove registros existentes.

**Próximo incremento recomendado:** catálogo somente leitura, com produtos compartilhados, disponibilidade por loja, migrations e testes de integração. Projetar autenticação e autorização por loja antes de adicionar operações administrativas de escrita.

## Autor e licença

Júnio Rosa · [LinkedIn](https://www.linkedin.com/in/j%C3%BAnio-rosa-94b5731b2/)

[Licença MIT](LICENSE)
