# Docker Compose and local bind persistence

## Status

Accepted

## Context

Local development needs reproducible service wiring and visible service-specific persistent data.

## Decision

Compose runs frontend, backend and PostgreSQL on an internal bridge; bind PostgreSQL 17 data to .dockerized-postgres at /var/lib/postgresql/data. Publish application ports only on loopback. Attach applications to a second web bridge for host port publishing; PostgreSQL also joins db_access for loopback publishing. POSTGRES_DATA_SOURCE optionally selects the project-scoped postgres_data named volume for fresh clones; the bind remains the default to retain existing data.

## Alternatives

Host-only setup; named database volumes; orchestration cluster.

## Consequences

Container removal preserves local data. Owners manage permissions, backups and explicit destructive resets. Compose does not establish production availability.
