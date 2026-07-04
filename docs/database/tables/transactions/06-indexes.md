# Indexes Specification

Version: 1.0

Status: Draft

Owner: Personal Finance App

Related Documents

- 04-relationships.md
- 05-constraints.md
- 07-reporting.md

---

# Purpose

This document defines the indexing strategy for the `transactions` table.

Indexes are designed to support the most common queries while minimizing unnecessary write overhead.

The objective is to provide fast reporting, efficient filtering and scalable performance without compromising data integrity.

---

# Indexing Philosophy

Indexes should be created based on query patterns rather than individual columns.

The strategy follows these principles.

- Optimize frequent queries.
- Minimize redundant indexes.
- Prefer composite indexes for reporting.
- Avoid indexing low-selectivity columns unless justified.
- Keep write performance acceptable.

---

# Primary Index

Every transaction is uniquely identified by its primary key.

```
PRIMARY KEY (id)
```

Purpose

- Fast lookup by identifier.
- Foreign key references.

---

# Foreign Key Indexes

Foreign key columns should be indexed because they are frequently used in joins.

Recommended indexes

```
from_account_id
```

```
to_account_id
```

```
category_id
```

```
salary_cycle_id
```

```
migration_batch_id
```

```
reconciliation_batch_id
```

---

# Reporting Indexes

Reporting queries frequently filter by transaction date.

```
(transaction_date)
```

Supports

- Daily reports
- Monthly reports
- Yearly reports
- Date range filtering

---

# Account History

Recommended composite indexes

```
(from_account_id, transaction_date)
```

```
(to_account_id, transaction_date)
```

Supports

- Account statements
- Cash history
- Transfer history

---

# Category Reports

```
(category_id, transaction_date)
```

Supports

- Expense by category
- Income by category
- Category trends

---

# Salary Cycle Reports

```
(salary_cycle_id, transaction_date)
```

Supports

- Salary cycle summaries
- Carry forward calculations
- Monthly reporting

---

# Transaction Type Reports

```
(transaction_type, transaction_date)
```

Supports

- Income reports
- Expense reports
- Transfer reports
- Adjustment reports

---

# Dashboard Queries

Typical dashboard widgets filter by recent transactions.

Recommended index

```
(transaction_date DESC)
```

Depending on PostgreSQL version, a standard B-tree index may already satisfy descending scans efficiently.

---

# Composite Index Strategy

The following composite indexes provide the greatest benefit.

| Index | Purpose |
|--------|---------|
| (transaction_date) | Date filtering |
| (transaction_type, transaction_date) | Type-based reports |
| (category_id, transaction_date) | Category reports |
| (salary_cycle_id, transaction_date) | Salary cycle reports |
| (from_account_id, transaction_date) | Source account history |
| (to_account_id, transaction_date) | Destination account history |

---

# Indexes Not Recommended

Version 1 should avoid indexes on

- description
- notes

These fields are rarely filtered.

If full-text search is introduced, dedicated search indexes should be used instead.

---

# Future Indexes

Future modules may require additional indexes.

Examples

- loan_id
- fund_id
- recurring_transaction_id
- merchant_id
- tag_id

These should be introduced together with their respective modules.

---

# Performance Considerations

Every additional index increases

- Storage usage
- INSERT cost
- UPDATE cost

Indexes should therefore be added only when they support real query patterns.

---

# Monitoring Strategy

Index usage should be reviewed periodically using PostgreSQL statistics.

Unused indexes should be removed.

Slow queries should be analyzed before introducing new indexes.

Performance optimization should be evidence-driven rather than assumption-driven.

---

# Design Principles

The indexing strategy follows these principles.

- Query-driven indexing
- Minimal redundancy
- Read optimization
- Acceptable write performance
- Scalability for future growth

---

# Final Statement

Indexes are an optimization layer built on top of the transaction ledger.

They improve query performance but never change the financial behavior of the application.

The ledger remains the authoritative source of financial data regardless of the indexing strategy.