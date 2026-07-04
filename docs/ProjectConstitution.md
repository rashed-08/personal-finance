# Project Constitution

## Purpose

This document defines the fundamental engineering principles governing the development of the Personal Finance App.

These principles apply to every feature, module, architectural decision, database change, and code contribution.

Whenever a conflict arises between implementation convenience and these principles, this document takes precedence.

---

# Mission

Build a long-term personal financial management platform that prioritizes:

- Financial correctness
- Simplicity
- Maintainability
- Transparency
- Documentation
- Long-term evolution

The project should remain understandable and maintainable many years after its initial implementation.

---

# Core Values

## 1. Financial Accuracy Above Everything

Financial correctness is the highest priority.

The system must never sacrifice correctness for convenience.

Money must always be explainable.

If a balance cannot be explained, the implementation is considered incorrect.

---

## 2. Documentation Before Implementation

Every significant feature should be documented before implementation.

Documentation includes:

- Requirements
- ADR
- Database
- API
- Business Workflow

Code should implement documented behavior rather than invent it.

---

## 3. Every Money Movement Must Be Traceable

Money never disappears.

Every financial movement shall have a recorded reason.

Examples:

Income

Expense

Transfer

Investment

Loan

Adjustment

Cash Reconciliation

Every balance must be reproducible from historical transactions.

---

## 4. Transactions Are the Source of Truth

Balances are derived.

Transactions are stored.

Reports are calculated.

No balance should become the primary source of financial truth.

---

## 5. Simplicity Over Cleverness

Readable code is preferred over clever code.

Future maintainers should understand code quickly.

Simple solutions should be chosen unless complexity provides clear value.

---

## 6. Business Logic Belongs to the Domain

Controllers should not contain business logic.

Repositories should not contain business rules.

Business decisions belong to the domain layer.

---

## 7. Database Changes Must Be Versioned

Every schema change must be implemented through Flyway migrations.

Manual production database modifications are prohibited.

The database schema must always be reproducible.

---

## 8. Historical Data Must Be Preserved

Historical financial records are valuable.

The system should avoid modifying historical data unless explicitly required.

Whenever possible, corrections should be represented by new transactions instead of overwriting existing data.

---

## 9. Reconciliation Must Be Transparent

Cash reconciliation should never silently modify balances.

Every reconciliation must preserve:

- previous balance
- calculated difference
- user decision
- resulting adjustment

The user should always understand how the final balance was obtained.

---

## 10. Reports Must Be Reproducible

Reports generated today should produce the same result tomorrow when the underlying data has not changed.

Financial reports should never depend on temporary application state.

---

## 11. Security Is a Default Requirement

Security is not an optional feature.

Sensitive information should remain protected by design.

Credentials must never be committed into version control.

Secrets belong outside the repository.

---

## 12. Test Critical Financial Logic

Financial calculations should be covered by automated tests.

The more money involved, the more important testing becomes.

---

## 13. Architecture Should Encourage Growth

The project should support future expansion without requiring major redesign.

Examples:

Budgeting

Investments

Assets

Notifications

Analytics

Cloud synchronization

Future modules should integrate naturally with the existing architecture.

---

## 14. Avoid Technical Debt When Practical

Short-term shortcuts should be minimized.

If technical debt is intentionally introduced, it should be documented.

Known limitations belong in:

KnownIssues.md

---

## 15. Automation Whenever Reasonable

Repetitive development tasks should be automated.

Examples:

Database migration

Build

Testing

Formatting

Deployment

Automation improves consistency and reduces human error.

---

## 16. Consistency Over Personal Preference

Consistency across the codebase is more valuable than individual coding style.

The project follows documented coding standards.

---

## 17. Single Responsibility

Every module should have one primary responsibility.

Large components should be decomposed into focused units.

---

## 18. Prefer Explicit Behavior

Hidden side effects should be avoided.

Important financial actions should be obvious from the code.

Explicit behavior improves reliability.

---

## 19. Long-Term Thinking

Every implementation should consider:

How will this behave five years from now?

Will this design still make sense?

Can future developers understand it?

Can it be extended?

---

## 20. Continuous Improvement

The architecture is expected to evolve.

Refactoring is encouraged when it improves:

Readability

Maintainability

Reliability

Performance

without sacrificing correctness.

---

# Engineering Rules

The following rules apply throughout the project.

- Documentation accompanies implementation.
- Financial correctness is non-negotiable.
- Business workflows are documented.
- Database schema is version-controlled.
- API contracts are documented.
- Critical business logic is tested.
- Repository history should remain meaningful.
- Every architectural decision should have a documented rationale.

---

# Decision Hierarchy

When conflicts occur, decisions should follow this priority:

Financial Accuracy

↓

Data Integrity

↓

Business Requirements

↓

Project Constitution

↓

Architecture Decisions (ADR)

↓

Coding Standards

↓

Implementation Convenience

---

# Constitutional Amendments

This document should change very rarely.

Any amendment should:

- have clear justification
- improve long-term maintainability
- preserve existing engineering principles

Constitution changes should be considered carefully because they affect the entire project.

---

# Final Statement

This Constitution defines the engineering culture of the Personal Finance App.

Technologies may change.

Frameworks may change.

Architecture may evolve.

But the principles described here should continue guiding the project throughout its lifetime.

The objective is not merely to build software, but to build software that remains trustworthy, understandable, and maintainable for many years.