import api from "../api/api";
import type { CashReconciliation } from "../types/cashReconciliation";
import type {
    AccountBalance,
    AccountStatement,
    CashFlowReport,
    CategoryReport,
    Dashboard,
    FundReportLine,
    IncomeExpenseReport,
    IncomeExpenseReportFilter,
    LoanReportLine,
    MonthlyReport,
    RecurringTransactionReportLine,
    SalaryCycleReport,
} from "../types/report";

const BASE_URL = "/reports";

export async function getDashboard(asOfDate?: string): Promise<Dashboard> {
    const response = await api.get<Dashboard>(`${BASE_URL}/dashboard`, { params: { asOfDate } });
    return response.data;
}

export async function getIncomeReport(filter: IncomeExpenseReportFilter = {}): Promise<IncomeExpenseReport> {
    const response = await api.get<IncomeExpenseReport>(`${BASE_URL}/income`, { params: filter });
    return response.data;
}

export async function getExpenseReport(filter: IncomeExpenseReportFilter = {}): Promise<IncomeExpenseReport> {
    const response = await api.get<IncomeExpenseReport>(`${BASE_URL}/expense`, { params: filter });
    return response.data;
}

export interface CategoryReportFilter {
    categoryId: string;
    fromDate?: string;
    toDate?: string;
}

export async function getCategoryReport(filter: CategoryReportFilter): Promise<CategoryReport> {
    const response = await api.get<CategoryReport>(`${BASE_URL}/category`, { params: filter });
    return response.data;
}

export async function getAccountBalances(asOfDate?: string, activeOnly = true): Promise<AccountBalance[]> {
    const response = await api.get<AccountBalance[]>(`${BASE_URL}/accounts/balances`, {
        params: { asOfDate, activeOnly },
    });
    return response.data;
}

export async function getAccountStatement(
    accountId: string,
    fromDate?: string,
    toDate?: string,
): Promise<AccountStatement> {
    const response = await api.get<AccountStatement>(`${BASE_URL}/accounts/${accountId}/statement`, {
        params: { fromDate, toDate },
    });
    return response.data;
}

export async function getFundReports(activeOnly = true): Promise<FundReportLine[]> {
    const response = await api.get<FundReportLine[]>(`${BASE_URL}/funds`, { params: { activeOnly } });
    return response.data;
}

export async function getFundReport(fundId: string): Promise<FundReportLine> {
    const response = await api.get<FundReportLine>(`${BASE_URL}/funds/${fundId}`);
    return response.data;
}

export async function getLoanReports(activeOnly = true): Promise<LoanReportLine[]> {
    const response = await api.get<LoanReportLine[]>(`${BASE_URL}/loans`, { params: { activeOnly } });
    return response.data;
}

export async function getLoanReport(loanId: string): Promise<LoanReportLine> {
    const response = await api.get<LoanReportLine>(`${BASE_URL}/loans/${loanId}`);
    return response.data;
}

export async function getSalaryCycleReport(salaryCycleId: string): Promise<SalaryCycleReport> {
    const response = await api.get<SalaryCycleReport>(`${BASE_URL}/salary-cycles/${salaryCycleId}`);
    return response.data;
}

export async function getMonthlyReport(yearMonth?: string): Promise<MonthlyReport> {
    const response = await api.get<MonthlyReport>(`${BASE_URL}/monthly`, { params: { yearMonth } });
    return response.data;
}

export async function getCashFlowReport(fromDate: string, toDate: string): Promise<CashFlowReport> {
    const response = await api.get<CashFlowReport>(`${BASE_URL}/cash-flow`, { params: { fromDate, toDate } });
    return response.data;
}

export interface CashReconciliationReportFilter {
    accountId?: string;
    fromDate?: string;
    toDate?: string;
}

export async function getCashReconciliationReport(
    filter: CashReconciliationReportFilter = {},
): Promise<CashReconciliation[]> {
    const response = await api.get<CashReconciliation[]>(`${BASE_URL}/cash-reconciliation`, { params: filter });
    return response.data;
}

export async function getRecurringTransactionReport(activeOnly = true): Promise<RecurringTransactionReportLine[]> {
    const response = await api.get<RecurringTransactionReportLine[]>(`${BASE_URL}/recurring-transactions`, {
        params: { activeOnly },
    });
    return response.data;
}
