# Personal Finance App

> A modern, self-hosted personal finance management system designed for long-term financial tracking, budgeting, investment planning, reporting, and cash reconciliation.

---

## Overview

Personal Finance App is a full-stack web application built for managing day-to-day finances with a focus on simplicity, accuracy, and long-term maintainability.

Unlike traditional expense trackers, this project is designed around real-life financial behavior.

Examples include:

- Salary received several days into a month
- Cash withdrawals from ATM
- Small untracked cash expenses
- Monthly cash reconciliation
- Internal account transfers
- Savings and investment funds
- Loans given and received
- Recurring donations
- Google Drive backup & restore

The goal is not only to record expenses, but to understand where money comes from, where it goes, and how financial health changes over time.

---

# Objectives

The application aims to provide:

- Daily expense tracking
- Income management
- Salary cycle tracking
- Cash flow analysis
- Monthly and yearly reports
- Budget comparison
- Category-wise analytics
- Investment tracking
- Loan management
- Fund management
- Financial dashboard
- Automatic cash reconciliation
- Cloud backup

---

# Project Goals

This project is being developed as a long-term personal finance platform rather than a simple expense tracker.

The architecture prioritizes:

- Maintainability
- Scalability
- Data consistency
- Clear documentation
- Automated testing
- Future extensibility

---

# Technology Stack

## Backend

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- Hibernate
- Maven
- Flyway
- PostgreSQL

---

## Frontend

- React
- TypeScript
- Vite
- React Router
- TanStack Query
- React Hook Form
- Material UI
- Axios
- Recharts

---

## Infrastructure

- Docker Desktop
- Docker Compose
- PostgreSQL
- pgAdmin
- Mailpit

---

## Development Tools

- IntelliJ IDEA
- VS Code
- DBeaver
- Bruno
- Git
- GitHub

---

# Architecture

The project follows a **Modular Monolith Architecture**.

```
                +------------------------+
                |      React (UI)        |
                +-----------+------------+
                            |
                     REST API
                            |
                +-----------v------------+
                |   Spring Boot Backend  |
                |                        |
                |  Accounts              |
                |  Transactions          |
                |  Reports               |
                |  Funds                 |
                |  Loans                 |
                |  Reconciliation        |
                +-----------+------------+
                            |
                     PostgreSQL
```

This architecture allows the project to remain simple during development while making future extraction into microservices possible if ever required.

---

# Major Features

## Income

- Salary
- Bonus
- Refund
- Other income

---

## Expenses

- Grocery
- Rent
- Utilities
- Internet
- Medicine
- Gym
- Transportation
- Recharge
- Shopping

---

## Accounts

- Cash
- Bank Accounts
- Mobile Banking
- Credit Card (Future)

---

## Transfers

Money can be transferred between accounts without being considered an expense.

Example:

```
Bank
↓

Emergency Fund
```

---

## Funds

Different savings goals.

Examples:

- Emergency Fund
- Zakat Fund
- Purchase Fund
- Investment Fund

---

## Loans

Supported workflows:

- Loan Given
- Loan Received
- Loan Repayment
- Loan Collection

---

## Reports

Examples:

- Monthly Expense Report
- Category Report
- Salary Report
- Investment Report
- Loan Report
- Cash Flow Report
- Yearly Comparison

---

## Cash Reconciliation

One of the core features of the application.

The application assumes users will not always record every small cash expense.

Instead:

1. ATM withdrawal is recorded.
2. Major expenses are recorded.
3. User periodically enters actual cash balance.
4. System calculates missing cash.
5. Difference can be categorized as:
   - Miscellaneous Expense
   - Transfer
   - Loan
   - Other Adjustment

---

## Backup

Planned support:

- Google Drive Backup
- Restore from Backup
- Database Export
- CSV Export

---

# Project Structure

```
PersonalFinanceApp/

backend/
frontend/
infra/
storage/
scripts/
docs/

README.md
Makefile
```

---

# Documentation

Project documentation lives inside:

```
docs/
```

Documentation includes:

- Requirements
- Database Design
- Architecture Decisions
- API Specifications
- Coding Standards
- UI Design
- Development Roadmap

---

# Development Workflow

Development follows an incremental sprint-based workflow.

Sprint 0

Infrastructure

↓

Sprint 1

Database Design

↓

Sprint 2

Core Modules

↓

Sprint 3

Reports

↓

Sprint 4

Advanced Features

---

# Branch Strategy

Main branch always represents a stable version.

Recommended branches:

```
main

develop

feature/*
```

---

# Versioning

Semantic Versioning will be followed.

Examples:

```
v0.1.0

v0.2.0

v1.0.0
```

---

# License

This project is currently intended for personal use.

Future licensing may change if the project becomes open source.

---

# Author

Rashed

Software Engineer

Bangladesh

---

# Project Status

Current Phase

Sprint 0

Status

Project Initialization

---

# Future Vision

This project is expected to evolve into a complete personal finance platform supporting:

- Investment Portfolio
- Asset Management
- Mobile Application
- Offline Support
- PWA
- Google Authentication
- Financial Insights
- AI-assisted Analytics
- Smart Notifications

---

> This repository represents a long-term engineering project focused on building a maintainable, well-documented, and production-quality personal finance management system.