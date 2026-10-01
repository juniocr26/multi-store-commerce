# PostgreSQL and Flyway

## Status

Accepted

## Context

Relational constraints and repeatable schema evolution are needed from the first store slice.

## Decision

Use PostgreSQL 17 and versioned Flyway SQL; Hibernate validates schema. Keep opt-in development fixtures outside schema migrations.

## Alternatives

H2 as primary database; Hibernate schema updates; Liquibase.

## Consequences

Real PostgreSQL integration tests require Docker. Migrations must be reviewed and remain immutable after application.
