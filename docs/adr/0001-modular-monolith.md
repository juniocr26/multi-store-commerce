# Modular monolith

## Status

Accepted

## Context

The portfolio needs clear business boundaries while remaining easy to run and debug.

## Decision

Deploy one backend, organized by business module. Implement only store now.

## Alternatives

Microservices; global controller/service/repository packages.

## Consequences

Simple transactions and deployment; module boundaries require discipline. Splitting services needs evidence and a later decision.
