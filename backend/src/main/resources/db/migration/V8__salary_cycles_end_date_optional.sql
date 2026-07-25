-- ============================================================================
-- V8 - Make salary_cycles.cycle_end_date Optional
-- ============================================================================
--
-- A salary cycle's end date is not known until the next salary payment
-- closes it (docs/business/SalaryWorkflow.md). V1 required cycle_end_date
-- NOT NULL, which made it impossible to represent an ongoing, still-open
-- cycle. The existing chk_salary_cycle_dates CHECK (start <= end) already
-- passes when end is NULL, so no constraint changes are needed there.
-- ============================================================================

ALTER TABLE salary_cycles
    ALTER COLUMN cycle_end_date DROP NOT NULL;
