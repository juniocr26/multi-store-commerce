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
