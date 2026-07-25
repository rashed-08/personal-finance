import type { AccountType } from "./account";
import type { FundType } from "./fund";
import type { LoanStatus, LoanType } from "./loan";
import type { RecurringTransaction } from "./recurringTransaction";
import type { Transaction, TransactionType } from "./transaction";

export interface LoanSummary {
    totalReceivable: number;
    totalPayable: number;
    netPosition: number;
    activeLoanCount: number;
}

export interface CategorySpending {
    categoryId: string;
    categoryName: string;
    totalSpent: number;
}

export interface Dashboard {
    totalBalance: number;
    cashBalance: number;
    totalFundBalance: number;
    loanSummary: LoanSummary;
    monthlyIncome: number;
    monthlyExpense: number;
    recentTransactions: Transaction[];
    dueRecurringTransactions: RecurringTransaction[];
    topSpendingCategories: CategorySpending[];
}

export interface CategoryBreakdown {
    categoryId: string;
    categoryName: string;
    total: number;
    transactionCount: number;
}

export interface DateBucket {
    date: string;
    total: number;
}

export interface IncomeExpenseReport {
    transactionType: TransactionType;
    total: number;
    transactionCount: number;
    byCategory: CategoryBreakdown[];
    byDate: DateBucket[];
}

export interface MonthlyAmount {
    yearMonth: string;
    total: number;
}

export interface CategoryReport {
    categoryId: string;
    categoryName: string;
    totalSpending: number;
    transactionCount: number;
    monthlyTrend: MonthlyAmount[];
    averagePerMonth: number;
}

export interface AccountBalance {
    accountId: string;
    accountName: string;
    accountType: AccountType;
    balance: number;
}

export interface StatementLine {
    transactionId: string;
    transactionDate: string;
    description: string | null;
    transactionType: TransactionType;
    signedAmount: number;
    runningBalance: number;
}

export interface AccountStatement {
    accountId: string;
    accountName: string;
    openingBalance: number;
    lines: StatementLine[];
    endingBalance: number;
}

export interface FundReportLine {
    fundId: string;
    fundName: string;
    fundType: FundType;
    targetAmount: number | null;
    allocatedAmount: number;
    usedAmount: number;
    remainingBalance: number;
    progressPercentage: number | null;
}

export interface LoanPaymentHistoryLine {
    transactionId: string;
    date: string;
    amount: number;
    description: string | null;
}

export interface LoanReportLine {
    loanId: string;
    name: string;
    loanType: LoanType;
    principalAmount: number;
    paidAmount: number;
    remainingAmount: number;
    loanStatus: LoanStatus;
    paymentHistory: LoanPaymentHistoryLine[];
}

export interface SalaryCycleReport {
    salaryCycleId: string;
    cycleName: string;
    startDate: string;
    endDate: string | null;
    closed: boolean;
    openingBalance: number;
    income: number;
    expenses: number;
    adjustments: number;
    closingBalance: number;
}

export interface MonthComparison {
    currentIncome: number;
    previousIncome: number;
    currentExpense: number;
    previousExpense: number;
}

export interface MonthlyReport {
    yearMonth: string;
    totalIncome: number;
    totalExpense: number;
    netCashFlow: number;
    expenseByCategory: CategoryBreakdown[];
    incomeByCategory: CategoryBreakdown[];
    comparisonToPreviousMonth: MonthComparison;
}

export interface CashFlowReport {
    fromDate: string;
    toDate: string;
    moneyIn: number;
    moneyOut: number;
    netCashFlow: number;
    totalTransferVolume: number;
}

export interface RecurringTransactionReportLine {
    recurringTransactionId: string;
    name: string;
    active: boolean;
    nextExecutionDate: string;
    lastExecutionDate: string | null;
    generatedCount: number;
    skippedCount: number;
}

export interface IncomeExpenseReportFilter {
    fromDate?: string;
    toDate?: string;
    salaryCycleId?: string;
    accountId?: string;
    categoryId?: string;
}
