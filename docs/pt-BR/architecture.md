# Arquitetura

[English](../en/architecture.md) · [README](../../README.pt-BR.md)

O navegador solicita `/api/v1/stores` na origem Angular. O servidor de desenvolvimento encaminha `/api/**` ao Spring Boot. O backend controla validação, transações, segurança e acesso ao PostgreSQL. O navegador não utiliza nomes DNS do Docker.

Um único backend é organizado por capacidade de negócio em `com.multistorecommerce`. O módulo `store` contém `api` (controller HTTP), `application` (serviço transacional, DTO e seed de desenvolvimento), `domain` (mapeamento JPA sem dependências HTTP) e `infrastructure` (repositório Spring Data). Segurança e erros compartilhados ficam em `configuration`. Não há framework CRUD genérico nem árvores vazias para módulos futuros.

A tabela `stores` possui UUID como chave primária, slug único minúsculo com hífens (80 caracteres), nome não vazio (160 caracteres) e indicador ativo obrigatório. A consulta filtra lojas ativas e ordena pelo slug único. Flyway cria o schema; Hibernate apenas valida. Migrations aplicadas são imutáveis. O SQL de seed fica fora das migrations, exige `dev` e `DEV_SEED_ENABLED=true` e insere UUIDs fictícios fixos de forma idempotente, sem sobrescrever registros existentes. Desativá-lo nunca remove dados.

A segurança é stateless, sem login, usuários gerados ou tokens. Apenas GET de lojas e probes mínimos são públicos; OpenAPI é habilitado e permitido somente em `dev`. Rotas não autorizadas e métodos de escrita retornam 403. CSRF está desabilitado nesta API somente leitura; revisar antes de autenticação por cookies ou mutações. CORS permite origens explícitas sem credenciais. Erros usam ProblemDetail; detalhes inesperados ficam nos logs do servidor, enquanto a resposta apresenta mensagem genérica. Liveness verifica o ciclo de vida da aplicação; readiness também consulta o banco. Detalhes de saúde e demais endpoints Actuator não são expostos.

Angular utiliza componentes standalone, rotas, HttpClient, signals e um serviço tipado. Um estado discriminado representa carregamento, falha e resultados. Não há biblioteca de estado global nem framework visual. Requisições são canceladas quando o componente é destruído. A interface está em inglês; a documentação é bilíngue.

Módulos planejados: identidade/acesso, catálogo, estoque, carrinho, pedido, pagamento e entrega. Notificações e relatórios poderão vir depois. `store` é a única capacidade de negócio implementada. Estratégias de autenticação, reserva de estoque e consistência de pagamentos ainda serão projetadas; consulte o [roadmap](roadmap.md). Não há isolamento entre empresas independentes.

Decisões aceitas: [monólito modular](../adr/0001-modular-monolith.md), [monorepo](../adr/0002-monorepo.md), [banco](../adr/0003-postgresql-flyway.md), [infraestrutura local](../adr/0004-compose-persistence.md).
