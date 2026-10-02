# Registro de verificação

[English](../en/verification.md)

Executado em 2026-10-01 no macOS arm64, com Docker Desktop / Engine 29.5.3 e toolchains temporárias Temurin 21.0.12.1+1 e Node 22.23.3. O Java 17 preinstalado não foi usado para compilar o projeto Java 21. Docker Desktop estava parado e foi iniciado para a validação.

| Verificação | Resultado |
| --- | --- |
| `./mvnw verify -Pintegration` | PASSOU: compilação/pacote backend, 5 testes MVC e 5 de integração PostgreSQL; nenhum ignorado |
| `npm ci` | PASSOU com npm 10.9.9 incluído no Node e lockfile versionável |
| `npm run build` | PASSOU: bundle Angular de produção, cerca de 231 kB iniciais sem compressão |
| `npm test` | PASSOU: 3 testes DOM cobrindo carregamento, vazio, sucesso, erro e nova tentativa |
| Auditoria npm durante instalação | 0 vulnerabilidades reportadas no momento da verificação |
| `docker compose --env-file .env.example config --quiet` | PASSOU; exatamente frontend/backend/postgres, bridge interna do banco e portas das aplicações em loopback |
| `docker compose --env-file .env.example up --build -d --wait` | PASSOU: ambas as imagens construídas, 3 serviços saudáveis |
| API no host e proxy frontend `/api/v1/stores` | PASSOU: mesmas 2 lojas fictícias ativas, ordenadas pelo slug |
| Chromium desktop (1280×800) e mobile (390×844) | PASSOU: lojas renderizadas, sem overflow horizontal nem erros de aplicação não tratados; screenshots inspecionadas visualmente |
| Estados no navegador | PASSOU: resposta atrasada mostra carregamento; respostas simuladas vazia/erro renderizam corretamente; nova tentativa retorna à API real |
| Live reload dos fontes Angular | PASSOU: alteração temporária de título apareceu automaticamente; fonte original restaurado e observado |
| Probes com banco indisponível | PASSOU: parar postgres mantém liveness 200/UP e resulta em readiness 503/DOWN; banco restaurado em seguida |
| Reinício não destrutivo de postgres/backend | PASSOU: readiness recupera e IDs/conteúdo das lojas permanecem idênticos |
| Usuários dos processos | PASSOU: backend UID 10001; frontend UID 1000 |

As primeiras execuções identificaram e corrigiram parsing de tag com digest no Testcontainers, falha de resolução do npm 10 e publicação de portas pelo Docker Engine em redes exclusivamente internas. Os testes foram repetidos após as correções. A distribuição Maven possui verificação SHA-256; imagens base e PostgreSQL possuem digests imutáveis.

O Compose utilizou `--env-file .env.example` para não criar nem sobrescrever um `.env` pessoal. Criou apenas containers de desenvolvimento deste projeto e dados em `.dockerized-postgres`. Não houve reset destrutivo do banco, commit, push ou deploy. Os serviços ficaram em execução para inspeção.

As verificações de navegador, indisponibilidade e reinício foram pontuais, usando ferramentas isoladas em armazenamento temporário; as suítes de regressão do projeto são Maven/JUnit e Angular/Vitest. As ferramentas temporárias não são dependências das aplicações. Não foram testados hosts Linux/Windows, outras arquiteturas de CPU, deploy de produção, carga/disponibilidade ou acessibilidade formal. Não restam verificações bloqueadas para a base local solicitada.

## Correção Docker — 2026-10-02

A validação atual está detalhada no [registro em inglês](../en/verification.md#docker-repair-verification--2026-10-02), com comandos, resultados e limitações.
Executada em macOS ARM64 com Docker Desktop Linux/aarch64, Engine 29.5.3 e
Compose 5.1.4. Todas as ferramentas de build/testes ficaram no Docker.

- Falha original reproduzida: falta de unzip faz o wrapper baixar tar.gz e
  comparar com o checksum do ZIP. O ZIP oficial foi verificado contra o SHA-512
  oficial; seu SHA-256 corresponde ao valor já configurado.
- Build sem camadas reutilizáveis e com namespace Maven vazio: passou.
- Stack isolada com redes e volume novo: três serviços saudáveis, Flyway V1
  aplicado, API vazia e proxy funcionando; HTML, JS e CSS retornaram 200.
- Cinco testes de API, cinco de integração PostgreSQL e três testes frontend:
  passaram, sem skips. Build Angular de produção passou.
- Arquivos simulando dependências e resultados compilados do host: excluídos.
  Mudança real de manifesto/lockfile reinstalou dependências no rebuild.
- Reinício e remoção/rebuild de containers, redes e imagens isolados mantiveram
  UUID e conteúdo inseridos no banco de teste.
- Volume removido/recriado, wrapper CRLF sem permissão no host, stop/up repetido
  e bind padrão novo com seed: passaram.
- Live reload confirmado por alteração no JavaScript servido, sem navegador.
- Imagens fixadas contêm índices Linux AMD64 e ARM64; execução em AMD64 ou hosts
  Windows/Linux não foi validada. Repetir os comandos isolados nessas plataformas.

Os recursos de validação foram removidos apenas pelo projeto
`commerce-validation-20261002`; o banco e os recursos existentes não foram alterados.
Caches BuildKit opcionais podem permanecer. Não há volume de dependências
frontend no fluxo suportado. O npm audit atual aponta duas entradas críticas
ligadas a piscina/@angular/build (GHSA-67c8-pqhq-4rmx), substituindo o resultado
histórico de zero vulnerabilidades acima. Atualizações de segurança de dependências
ficaram fora desta correção Docker. Stripe e código de aplicação não foram alterados.
