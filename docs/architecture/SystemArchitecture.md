# System Architecture

## Purpose

This document defines the overall architecture of the Personal Finance App.

It describes how the application is organized, how modules communicate, and the architectural principles that guide implementation.

This document serves as the technical blueprint for backend, frontend, database, deployment, and future evolution.

---

# Architecture Style

The application follows a:

> Modular Monolith Architecture

Characteristics

- Single deployable application
- Independent business modules
- Shared database
- Internal module boundaries
- Clear separation of responsibilities
- Future microservice migration path

This architecture provides simplicity during early development while remaining scalable.

---

# High-Level Architecture

```

                        Browser
                           │
                    React + TypeScript
                           │
                      REST API (HTTPS)
                           │
                    Spring Boot Backend
                           │
     ┌──────────────────────────────────────┐
     │          Business Modules            │
     │                                      │
     │ Accounts                             │
     │ Transactions                         │
     │ Categories                           │
     │ Salary                               │
     │ Funds                                │
     │ Loans                                │
     │ Cash Reconciliation                  │
     │ Reports                              │
     │ Google Drive Backup                  │
     └──────────────────────────────────────┘
                           │
                     PostgreSQL Database
                           │
                    File Storage / Backup
```

---

# Technology Stack

## Frontend

- React
- TypeScript
- Vite
- React Router
- TanStack Query
- Axios
- React Hook Form
- Zod

---

## Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Validation
- Spring Data JPA
- Flyway
- PostgreSQL Driver

---

## Database

- PostgreSQL

---

## Infrastructure

- Docker Compose
- Docker Desktop
- pgAdmin
- Mailpit
- DBeaver

---

# Architectural Principles

The system follows these principles.

- Business-first design
- Domain-driven thinking
- Ledger-based accounting
- Immutable financial history
- Modular boundaries
- Single source of truth
- API-first communication
- Configuration over hardcoding

---

# Backend Modules

The backend is divided into independent modules.

```
accounts
categories
transactions
salary
funds
loans
cash
reports
backup
settings
```

Each module owns:

- Controller
- Service
- Domain
- Repository
- DTOs
- Validation
- Exceptions

---

# Package Structure

```
io.rashed.finance

config

common

shared

modules

    accounts

    categories

    transactions

    salary

    funds

    loans

    cash

    reports

    backup

    settings
```

Each module should remain independent.

---

# Layered Architecture

Each module follows

```
Controller

↓

Application Service

↓

Domain Service

↓

Repository

↓

Database
```

Controllers never contain business logic.

Repositories never contain business rules.

---

# Domain Layer

The domain layer owns:

- business rules
- validations
- calculations
- financial policies

Everything related to money belongs here.

---

# Application Layer

Responsible for

- orchestration
- transaction management
- security
- workflow coordination

---

# Persistence Layer

Responsible only for

- reading
- writing
- querying

Repositories never calculate balances.

---

# REST API

Communication between frontend and backend occurs through REST.

Frontend never accesses the database directly.

---

# Request Flow

```
React

↓

REST

↓

Controller

↓

Service

↓

Domain

↓

Repository

↓

PostgreSQL
```

---

# Transaction Engine

Every financial action becomes a transaction.

Examples

Salary

Expense

Transfer

Loan

Fund

Adjustment

The transaction module acts as the financial ledger.

---

# Reporting Engine

Reports never own data.

Reports calculate from:

Transactions

Salary Cycles

Categories

Funds

Loans

Cash Reconciliation

---

# Balance Strategy

Balances are calculated.

Cached balances may exist.

Transactions remain authoritative.

---

# Configuration

Configuration should be externalized.

Profiles

```
local
dev
prod
```

Sensitive information should never be committed.

---

# Logging

Logging levels

ERROR

WARN

INFO

DEBUG

Financial operations should generate audit logs.

---

# Validation

Validation occurs in multiple layers.

Client

↓

DTO Validation

↓

Business Validation

↓

Database Constraints

---

# Exception Handling

Global exception handler.

Standard error response.

Consistent error codes.

Readable messages.

---

# Authentication

Version 1

Single User

Future

Spring Security

JWT

OAuth

Google Login

---

# Authorization

Initially unnecessary.

Architecture should allow future role support.

Examples

Owner

Admin

Viewer

---

# Google Drive Backup

Separate module.

Responsibilities

- export
- import
- backup history
- restore

Business modules remain unaware of Google APIs.

---

# File Storage

Application-generated files

```
storage/

uploads

exports

backups

logs
```

---

# Deployment

Development

Docker Compose

Production

Single Docker Container

Future

Kubernetes

---

# Database Migration

Flyway is the only supported migration tool.

Schema changes must never be manual.

---

# Testing Strategy

Unit Tests

Integration Tests

Repository Tests

API Tests

End-to-End Tests

---

# Monitoring

Future support

Spring Boot Actuator

Prometheus

Grafana

Health Checks

---

# Performance Principles

- avoid unnecessary queries
- pagination
- indexing
- lazy loading where appropriate
- batch processing for reports

Correctness has higher priority than premature optimization.

---

# Future Evolution

The architecture should support extracting modules into independent services.

Potential candidates

Reports

Google Backup

Notifications

AI Insights

without major refactoring.

---

# Module Communication

Modules communicate through service interfaces.

Direct database access across modules should be avoided whenever practical.

Example

Reports

↓

Transaction Service

↓

Transaction Repository

instead of

Reports

↓

Transaction Table

---

# Security Principles

- validate every request
- sanitize input
- never trust client data
- protect backups
- encrypt secrets
- use HTTPS in production

---

# Documentation

Every architectural change should produce

- ADR
- migration
- documentation update

Architecture documentation is part of the codebase.

---

# Final Statement

The Personal Finance App adopts a modular monolith architecture centered around a ledger-based financial engine.

Business rules remain isolated within domain modules.

Financial history remains immutable.

Modules remain independent.

Future growth—including multi-user support, microservices, AI insights, and advanced financial analysis—should be achievable without redesigning the core architecture.