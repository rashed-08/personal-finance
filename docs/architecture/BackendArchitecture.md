# Backend Architecture

## Purpose

This document defines the internal architecture of the Spring Boot backend.

It establishes how source code is organized, how modules communicate, and where business logic belongs.

The objective is to keep the codebase maintainable, scalable, and easy to understand as the application grows.

---

# Technology Stack

- Java 21
- Spring Boot
- Spring MVC
- Spring Validation
- Spring Data JPA
- Flyway
- PostgreSQL
- Maven

---

# Architectural Style

The backend follows:

- Modular Monolith
- Package by Feature
- Layered Architecture
- Domain-Oriented Design
- Ledger-Based Financial Engine

---

# Source Tree

```
src/main/java

io.rashed.finance

    config

    common

    shared

    modules
```

Everything business-related lives inside **modules**.

---

# Module Structure

Every business module follows the same layout.

```
modules

    transactions

        controller

        service

        domain

        repository

        entity

        dto

        mapper

        validator

        exception
```

Example:

```
modules

    accounts

    categories

    salary

    funds

    loans

    reports

    cash

    backup

    settings
```

---

# Why Package by Feature?

Benefits:

- better cohesion
- lower coupling
- easier navigation
- simpler refactoring
- easier extraction to microservices later

---

# Layer Responsibilities

## Controller

Responsible for

- REST endpoints
- request validation
- response formatting

Controllers must remain thin.

Controllers must never contain business logic.

---

## Service

Coordinates use cases.

Responsibilities

- orchestration
- transactions
- calling repositories
- invoking domain services

Services should not become "God Classes".

---

## Domain

Owns business rules.

Examples

- salary cycle calculation
- carry forward calculation
- reconciliation rules
- accounting rules
- financial validation

If a rule affects money,

it belongs here.

---

## Repository

Responsible only for persistence.

Responsibilities

- save
- update
- delete
- query

Repositories never calculate balances.

Repositories never perform business decisions.

---

## Entity

Represents database tables.

Entities should remain persistence models.

Avoid placing business logic inside entities.

---

## DTO

Used for communication with clients.

Never expose entities directly.

Examples

- CreateTransactionRequest
- UpdateAccountRequest
- SalaryResponse

---

## Mapper

Responsible for

DTO

↓

Entity

↓

Domain Object

↓

DTO

Mapping logic stays isolated.

---

## Validator

Contains complex validation that cannot be expressed using annotations.

Example

- salary cycle validation
- fund balance validation
- loan repayment validation

---

## Exception

Each module owns its exceptions.

Example

```
TransactionNotFoundException

InsufficientFundException

InvalidSalaryCycleException
```

---

# Shared Package

Shared code should be minimal.

Examples

- BaseEntity
- ApiResponse
- ErrorResponse
- Constants
- Utilities

Business logic must never move into shared.

---

# Common Package

Contains application-wide concerns.

Examples

- configuration
- security
- interceptors
- logging
- auditing
- filters

---

# Configuration

Configuration classes

```
config

DatabaseConfig

JacksonConfig

CorsConfig

OpenApiConfig

FlywayConfig
```

No business logic belongs here.

---

# Domain Rules

Every module owns its own business rules.

Example

Transaction module

owns

- expense rules
- transfer rules
- adjustment rules

Reports never implement transaction rules.

---

# Module Communication

Modules communicate through services.

Example

```
Reports

↓

TransactionService

↓

TransactionRepository
```

Avoid

```
Reports

↓

TransactionRepository
```

Business rules should always be respected.

---

# Dependency Direction

Allowed

```
Controller

↓

Service

↓

Domain

↓

Repository
```

Never

```
Repository

↓

Service
```

Never

```
Entity

↓

Repository
```

---

# Transactions

Spring Transactions belong in the Service Layer.

Repositories should remain transaction-agnostic.

---

# Validation Flow

```
Client

↓

DTO Validation

↓

Business Validation

↓

Database Constraint
```

Multiple validation layers improve reliability.

---

# Error Handling

Global Exception Handler

↓

Standard Error Response

↓

Client

Every API should return consistent error payloads.

---

# Naming Conventions

Controllers

```
TransactionController
```

Services

```
TransactionService
```

Repositories

```
TransactionRepository
```

Entities

```
Transaction
```

DTOs

```
CreateTransactionRequest

TransactionResponse
```

Mappers

```
TransactionMapper
```

Validators

```
TransactionValidator
```

---

# Package Visibility

Default package visibility should be preferred whenever possible.

Expose only what other modules require.

Reduce unnecessary public classes.

---

# Utility Classes

Utility classes should remain rare.

If a utility understands business concepts,

it belongs inside a module instead.

---

# Logging

Log

- application startup
- financial operations
- reconciliation
- backup
- restore
- unexpected failures

Avoid excessive logging.

Never log secrets.

---

# Database Access

All database interaction occurs through repositories.

Controllers never access repositories.

Domain objects never access repositories directly.

---

# Future Modules

The architecture should allow additional modules such as

- Budget
- Investment
- Notifications
- AI Insights
- Multi-Currency
- Tax
- Recurring Transactions

without restructuring existing modules.

---

# Code Quality Principles

- Small classes
- Small methods
- High cohesion
- Low coupling
- Constructor injection
- Prefer composition over inheritance
- Avoid circular dependencies
- Keep business logic inside the domain layer

---

# Testing Strategy

Each module should contain

- Unit Tests
- Repository Tests
- Integration Tests

Business rules must be tested independently from the database whenever practical.

---

# Final Statement

The backend architecture emphasizes modularity, clear ownership, and long-term maintainability.

Every business capability owns its controllers, services, repositories, domain logic, validation, and exceptions.

Financial correctness takes precedence over implementation convenience, ensuring that the backend remains understandable, extensible, and reliable as the application evolves.