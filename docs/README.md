# Project Documentation

Welcome to the documentation for the **Personal Finance App**.

This directory contains all technical and functional documentation related to the project.

The goal is to document every important architectural decision, business rule, database design, and development guideline so the project remains maintainable for years.

---

# Documentation Philosophy

This project follows a **Documentation First** approach.

Before implementing a feature, the following should be documented whenever applicable:

- Business requirements
- Technical design
- Database impact
- API contract
- Architecture decisions

This ensures the implementation follows an agreed design instead of evolving without structure.

---

# Documentation Structure

```
docs/

├── README.md
│
├── requirements/
│
├── database/
│
├── api/
│
├── adr/
│
├── diagrams/
│
├── ui/
│
├── CodingStandards.md
├── ProjectConstitution.md
├── CHANGELOG.md
└── KnownIssues.md
```

---

# Documentation Reading Order

For someone joining the project (or for future reference), the recommended reading order is:

1. Vision
2. Functional Requirements
3. Non-Functional Requirements
4. Roadmap
5. Project Constitution
6. Architecture Decision Records (ADR)
7. Database Design
8. API Specifications
9. Coding Standards

Following this order provides the necessary business context before diving into implementation details.

---

# Requirements

Location

```
requirements/
```

Purpose

Defines **what** the system should do.

Contents

- Vision
- Functional Requirements
- Non-Functional Requirements
- Development Roadmap

---

## Vision

Defines:

- Project goals
- Long-term direction
- Design philosophy

---

## Functional Requirements

Defines all user-visible features.

Examples:

- Expense Management
- Income Tracking
- Salary Cycle
- Reports
- Loans
- Funds
- Cash Reconciliation

---

## Non-Functional Requirements

Defines quality attributes.

Examples:

- Performance
- Security
- Reliability
- Scalability
- Maintainability

---

## Roadmap

Contains planned milestones and development phases.

---

# Database Documentation

Location

```
database/
```

Purpose

Defines how financial data is stored.

Contents

- ER Diagram
- Schema Design
- Naming Convention
- Transaction Flow
- Salary Cycle
- Cash Reconciliation

---

## ERD

Entity relationship diagrams.

---

## Schema

Detailed explanation of every table.

Including:

- Purpose
- Relationships
- Constraints
- Indexes

---

## Naming Convention

Defines naming rules.

Examples:

- Table names
- Column names
- Foreign keys
- Constraints
- Indexes

---

## Transaction Flow

Documents how money moves through the system.

Examples:

- Income
- Expense
- Transfer
- Investment
- Loan
- Adjustment

---

## Salary Cycle

Explains why salary periods are different from calendar months.

Example:

Salary received on the 10th of every month while expenses continue from the previous salary.

---

## Cash Reconciliation

Documents the automatic reconciliation workflow.

Examples:

ATM Withdrawal

↓

Recorded Expenses

↓

Actual Cash

↓

Difference

↓

Adjustment

---

# API Documentation

Location

```
api/
```

Purpose

Defines REST API contracts.

Examples

- Authentication
- Accounts
- Transactions
- Reports

Each API document should include:

- Endpoint
- Request
- Response
- Validation
- Error Cases

---

# Architecture Decision Records (ADR)

Location

```
adr/
```

Purpose

Explains **why** technical decisions were made.

Examples

- Why Modular Monolith?
- Why PostgreSQL?
- Why React?
- Why Docker?

Each ADR should include:

- Context
- Decision
- Alternatives Considered
- Consequences

---

# UI Documentation

Location

```
ui/
```

Purpose

Contains user interface documentation.

Future contents:

- Wireframes
- Mockups
- User Flow
- Dashboard Design
- Mobile Layout
- Responsive Behavior

---

# Diagrams

Location

```
diagrams/
```

Purpose

Visual representation of the system.

Examples:

- Architecture Diagram
- Database Diagram
- Module Diagram
- Deployment Diagram
- Sequence Diagram

---

# Coding Standards

Document

```
CodingStandards.md
```

Defines development conventions.

Examples

- Java conventions
- React conventions
- Naming
- DTO rules
- Exception handling
- Package structure

---

# Project Constitution

Document

```
ProjectConstitution.md
```

Contains the project's engineering principles.

Examples

- Financial data must never be silently modified.
- Database schema changes require Flyway migrations.
- Every money movement must be represented as a transaction.
- Documentation must be updated before implementing major features.

---

# CHANGELOG

Document

```
CHANGELOG.md
```

Records notable changes for every version.

Recommended format:

```
Version

Added

Changed

Fixed

Removed
```

---

# Known Issues

Document

```
KnownIssues.md
```

Tracks limitations, technical debt, and unresolved problems.

Examples:

- Performance concerns
- Future refactoring
- Known bugs
- Temporary workarounds

---

# Documentation Maintenance Rules

Documentation should evolve together with the codebase.

Whenever a significant feature is added:

- Update requirements if behavior changes.
- Update database documentation if schema changes.
- Update API documentation if endpoints change.
- Add an ADR for major architectural decisions.
- Update the roadmap if priorities change.

Documentation should never become outdated.

---

# Writing Guidelines

Documentation should be:

- Clear
- Concise
- Version-controlled
- Easy to navigate
- Focused on long-term maintainability

Avoid documenting implementation details that are likely to change frequently.

Instead, document the reasoning behind decisions and the expected system behavior.

---

# Future Documentation

Additional documentation may include:

- Deployment Guide
- Backup & Restore Guide
- Security Guide
- Performance Tuning Guide
- Disaster Recovery Plan
- Monitoring Guide
- Troubleshooting Guide
- Testing Guide
- Release Process
- Contribution Guide

---

# Final Note

Good software is not defined only by clean code.

Well-structured documentation preserves knowledge, explains decisions, and makes the project easier to maintain over time.

This documentation is considered part of the source code and should be maintained with the same level of care.