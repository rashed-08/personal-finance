import { useQuery } from "@tanstack/react-query";

import {
    getAccountBalances,
    getAccountStatement,
    getCashFlowReport,
    getCashReconciliationReport,
    getCategoryReport,
    getDashboard,
    getExpenseReport,
    getFundReport,
    getFundReports,
    getIncomeReport,
    getLoanReport,
    getLoanReports,
    getMonthlyReport,
    getRecurringTransactionReport,
    getSalaryCycleReport,
} from "../services/report.service";
import type {
    CashReconciliationReportFilter,
    CategoryReportFilter,
} from "../services/report.service";
import type { IncomeExpenseReportFilter } from "../types/report";

const QUERY_KEY = "reports";

export function useDashboard(asOfDate?: string) {
    return useQuery({
        queryKey: [QUERY_KEY, "dashboard", asOfDate],
        queryFn: () => getDashboard(asOfDate),
    });
}

export function useIncomeReport(filter: IncomeExpenseReportFilter = {}) {
    return useQuery({
        queryKey: [QUERY_KEY, "income", filter],
        queryFn: () => getIncomeReport(filter),
    });
}

export function useExpenseReport(filter: IncomeExpenseReportFilter = {}) {
    return useQuery({
        queryKey: [QUERY_KEY, "expense", filter],
        queryFn: () => getExpenseReport(filter),
    });
}

/** Fetches only the selected side (income or expense) — avoids firing both requests just to toggle a view. */
export function useIncomeOrExpenseReport(type: "INCOME" | "EXPENSE", filter: IncomeExpenseReportFilter = {}) {
    return useQuery({
        queryKey: [QUERY_KEY, type === "INCOME" ? "income" : "expense", filter],
        queryFn: () => (type === "INCOME" ? getIncomeReport(filter) : getExpenseReport(filter)),
    });
}

export function useCategoryReport(filter: CategoryReportFilter | undefined) {
    return useQuery({
        queryKey: [QUERY_KEY, "category", filter],
        queryFn: () => getCategoryReport(filter as CategoryReportFilter),
        enabled: !!filter?.categoryId,
    });
}

export function useAccountBalances(asOfDate?: string, activeOnly = true) {
    return useQuery({
        queryKey: [QUERY_KEY, "account-balances", asOfDate, activeOnly],
        queryFn: () => getAccountBalances(asOfDate, activeOnly),
    });
}

export function useAccountStatement(accountId: string | undefined, fromDate?: string, toDate?: string) {
    return useQuery({
        queryKey: [QUERY_KEY, "account-statement", accountId, fromDate, toDate],
        queryFn: () => getAccountStatement(accountId as string, fromDate, toDate),
        enabled: !!accountId,
    });
}

export function useFundReports(activeOnly = true) {
    return useQuery({
        queryKey: [QUERY_KEY, "funds", activeOnly],
        queryFn: () => getFundReports(activeOnly),
    });
}

export function useFundReport(fundId: string | undefined) {
    return useQuery({
        queryKey: [QUERY_KEY, "fund", fundId],
        queryFn: () => getFundReport(fundId as string),
        enabled: !!fundId,
    });
}

export function useLoanReports(activeOnly = true) {
    return useQuery({
        queryKey: [QUERY_KEY, "loans", activeOnly],
        queryFn: () => getLoanReports(activeOnly),
    });
}

export function useLoanReport(loanId: string | undefined) {
    return useQuery({
        queryKey: [QUERY_KEY, "loan", loanId],
        queryFn: () => getLoanReport(loanId as string),
        enabled: !!loanId,
    });
}

export function useSalaryCycleReport(salaryCycleId: string | undefined) {
    return useQuery({
        queryKey: [QUERY_KEY, "salary-cycle", salaryCycleId],
        queryFn: () => getSalaryCycleReport(salaryCycleId as string),
        enabled: !!salaryCycleId,
    });
}

export function useMonthlyReport(yearMonth?: string) {
    return useQuery({
        queryKey: [QUERY_KEY, "monthly", yearMonth],
        queryFn: () => getMonthlyReport(yearMonth),
    });
}

export function useCashFlowReport(fromDate: string | undefined, toDate: string | undefined) {
    return useQuery({
        queryKey: [QUERY_KEY, "cash-flow", fromDate, toDate],
        queryFn: () => getCashFlowReport(fromDate as string, toDate as string),
        enabled: !!fromDate && !!toDate,
    });
}

export function useCashReconciliationReport(filter: CashReconciliationReportFilter = {}) {
    return useQuery({
        queryKey: [QUERY_KEY, "cash-reconciliation", filter],
        queryFn: () => getCashReconciliationReport(filter),
    });
}

export function useRecurringTransactionReport(activeOnly = true) {
    return useQuery({
        queryKey: [QUERY_KEY, "recurring-transactions", activeOnly],
        queryFn: () => getRecurringTransactionReport(activeOnly),
    });
}
