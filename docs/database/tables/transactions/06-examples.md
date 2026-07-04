# Part 6 — Business Examples

---

## 6.1 Salary Received

### Scenario

The user receives a monthly salary of **80,000 BDT** into their primary bank account.

### Transaction

| Field | Value |
|--------|-------|
| transaction_type | INCOME |
| amount | 80000 |
| from_account | External |
| to_account | City Bank |
| category | Salary |
| salary_cycle | July 2026 |

### Ledger

```
External
      │
      ▼
City Bank
+80,000
```

---

## 6.2 Grocery Shopping

### Scenario

The user spends **2,300 BDT** from cash.

### Transaction

| Field | Value |
|--------|-------|
| transaction_type | EXPENSE |
| amount | 2300 |
| from_account | Cash |
| to_account | External |
| category | Groceries |

### Ledger

```
Cash
   │
   ▼
External
-2,300
```

---

## 6.3 ATM Withdrawal

### Scenario

The user withdraws **20,000 BDT** from the bank.

### Transaction

| Field | Value |
|--------|-------|
| transaction_type | TRANSFER |
| amount | 20000 |
| from_account | City Bank |
| to_account | Cash |

### Ledger

```
City Bank

↓

Cash
```

Net Worth remains unchanged.

---

## 6.4 Bank Transfer

### Scenario

Move **15,000 BDT** from BKash to Bank.

```
BKash

↓

City Bank
```

Transaction Type

```
TRANSFER
```

---

## 6.5 Loan Given

### Scenario

The user lends **150,000 BDT** to their wife.

### Transaction

| Field | Value |
|--------|-------|
| transaction_type | EXPENSE |
| amount | 150000 |
| from_account | Bank |
| category | Loan Given |

Business Relationship

```
Transaction

↓

Loan

↓

Borrower
```

The loan module records the outstanding balance.

---

## 6.6 Loan Repayment

### Scenario

The borrower repays **25,000 BDT**.

### Transaction

| Field | Value |
|--------|-------|
| transaction_type | INCOME |
| amount | 25000 |
| to_account | Bank |
| category | Loan Repayment |

Loan Balance

```
150000

-

25000

=

125000
```

---

## 6.7 Emergency Fund Allocation

### Scenario

Allocate **10,000 BDT** to the Emergency Fund.

### Transaction

```
Cash

↓

Emergency Fund Allocation
```

Business Context

```
Fund Relationship
```

Available Cash decreases.

Fund balance increases.

---

## 6.8 Fund Withdrawal

### Scenario

Withdraw **5,000 BDT** from the Emergency Fund.

```
Emergency Fund

↓

Cash
```

Fund balance decreases.

Cash increases.

---

## 6.9 Cash Reconciliation

### Scenario

ATM Withdrawal

```
20,000
```

Recorded Expenses

```
5,000
```

Expected Cash

```
15,000
```

Actual Cash Count

```
13,800
```

Difference

```
1,200
```

Adjustment Transaction

| Field | Value |
|--------|-------|
| transaction_type | ADJUSTMENT |
| amount | 1200 |
| adjustment_reason | CASH_RECONCILIATION |

Historical transactions remain unchanged.

---

## 6.10 Carry Forward

### July

Income

```
80,000
```

Expense

```
45,000
```

Transfer

```
10,000
```

Adjustment

```
500
```

Closing Balance

```
35,500
```

Carry Forward

```
↓

August Opening Balance
```

---

## 6.11 Opening Balance

### Scenario

Create a new Bank Account.

Initial Deposit

```
50,000
```

Transaction Type

```
OPENING_BALANCE
```

Only one opening balance transaction is allowed per account.

---

## 6.12 Google Keep Migration

### Scenario

Import historical expenses from Google Keep.

Imported Transaction

```
Date

Amount

Category

Notes
```

Transaction Type

```
MIGRATION
```

Reports treat imported transactions exactly like manually entered transactions.

---

## 6.13 Recurring Transaction

### Monthly Internet Bill

Recurring Template

```
Internet

1500

Every Month
```

Scheduler

↓

Creates

```
EXPENSE
```

Normal Transaction

↓

Ledger

The generated transaction becomes independent of the recurring template.

---

## 6.14 Invalid Transaction Examples

### Example 1

```
Amount = 0
```

Rejected

---

### Example 2

```
Negative Amount
```

Rejected

---

### Example 3

```
Transfer

Cash

↓

Cash
```

Rejected

---

### Example 4

Expense without Category

Rejected

---

### Example 5

Income without Destination Account

Rejected

---

## 6.15 Complete Salary Cycle Example

```
Opening Balance

30,000

↓

Salary

80,000

↓

Groceries

18,000

↓

Rent

20,000

↓

ATM Withdrawal

15,000

↓

Cash Adjustment

500

↓

Closing Balance

71,500

↓

Carry Forward

↓

Next Salary Cycle
```

---

## 6.16 Money Flow Summary

```
External Income

↓

Accounts

↓

Transfers

↓

Expenses

↓

Carry Forward

↓

Next Salary Cycle
```

Loans

Funds

Recurring Transactions

Cash Reconciliation

all operate on top of the same financial ledger.

---

## 6.17 Final Example

Every business feature in the application ultimately becomes one or more ledger transactions.

```
Salary
Market
ATM
Loan
Fund
Recurring
Migration
Cash Adjustment

↓

Transactions

↓

Reports

↓

Dashboard
```

The ledger remains the single source of truth for the entire application.

---

# Final Statement

The examples in this document are normative.

Future implementations (Database, Flyway, Java Domain Model, REST API and React UI) should follow these scenarios unless an Architecture Decision Record (ADR) explicitly changes the behavior.
