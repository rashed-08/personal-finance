# Transactions Table Documentation

The `transactions` table is the **central financial ledger** of the Personal Finance Application.

Every financial event—whether it is income, expense, transfer, loan activity, fund allocation, cash reconciliation, recurring transaction, or historical migration—is ultimately represented as one or more ledger transactions.

This documentation is organized into multiple focused documents to keep the specification modular, maintainable, and easy to navigate.

---

# Documentation Structure

| Document | Description |
|----------|-------------|
| [01-overview.md](01-overview.md) | Design philosophy, ledger principles, table overview, columns, and enumerations |
| [02-transaction-types.md](02-transaction-types.md) | Supported transaction types, money flow, and business semantics |
| [03-business-rules.md](03-business-rules.md) | Validation rules, lifecycle, salary cycle, cash reconciliation, and carry-forward logic |
| [04-relationships.md](04-relationships.md) | Relationships with accounts, categories, salary cycles, loans, funds, and other domains |
| [05-constraints.md](05-constraints.md) | Database constraints, integrity rules, and validation responsibilities |
| [06-indexes.md](06-indexes.md) | Indexing strategy, performance considerations, and query optimization |
| [07-reporting.md](07-reporting.md) | Reporting rules, balance calculations, and reporting principles |
| [08-examples.md](08-examples.md) | Practical business scenarios and real-world transaction examples |
| [09-future-design.md](09-future-design.md) | Future extensibility, architectural evolution, and long-term design goals |
| [10-architecture-review.md](10-architecture-review.md) | Architectural assessment, design trade-offs, and implementation recommendations |

---

# Recommended Reading Order

For first-time readers, it is recommended to follow the documents in the following order:

1. Overview
2. Transaction Types
3. Business Rules
4. Relationships
5. Constraints
6. Indexes
7. Reporting
8. Examples
9. Future Design
10. Architecture Review

Following this sequence provides a gradual understanding of the transaction ledger, from fundamental concepts to implementation details.

---

# Related Documentation

- [Domain Model](../../../domain/DomainModel.md)
- [Financial Accounting Model](../../../domain/FinancialAccountingModel.md)
- [Salary Workflow](../../../business/SalaryWorkflow.md)
- [Cash Reconciliation Workflow](../../../business/CashReconciliationWorkflow.md)
- [Carry Forward Workflow](../../../business/CarryForwardWorkflow.md)
- [System Architecture](../../../architecture/SystemArchitecture.md)
- [Database Schema](../../../database/Schema.md)

---

# Key Principles

The transaction ledger is built on the following principles:

- Ledger-first architecture
- Single source of truth
- Immutable financial history
- Calculated balances instead of stored balances
- Explicit money movement
- Clear separation between business domains and financial records
- Auditable and extensible design

These principles guide every module within the Personal Finance Application.

---

# Final Statement

The `transactions` table is the foundation of the Personal Finance Application.

Every financial module ultimately records, derives, and reports financial information through this ledger, making it the single authoritative source of truth for the entire system.