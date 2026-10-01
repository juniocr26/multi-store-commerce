# Angular and Spring Boot monorepo

## Status

Accepted

## Context

The first increment needs an understandable frontend/backend integration and coordinated API changes.

## Decision

Keep one standalone Angular application and one Maven Spring Boot application in a monorepo.

## Alternatives

Separate repositories; server-rendered application.

## Consequences

One checkout and coordinated changes; independent toolchains and Docker contexts remain necessary.
