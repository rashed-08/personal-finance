-- ============================================================================
-- V4 - Seed Default Funds
-- ============================================================================

INSERT INTO funds (
    name,
    fund_type,
    description,
    is_active
)
VALUES

-- -------------------------------------------------------------------------
-- Emergency Fund
-- -------------------------------------------------------------------------

(
    'Emergency Fund',
    'EMERGENCY',
    'Reserved for unexpected financial emergencies.',
    TRUE
),

-- -------------------------------------------------------------------------
-- Savings Fund
-- -------------------------------------------------------------------------

(
    'General Savings',
    'SAVINGS',
    'General-purpose savings for future use.',
    TRUE
),

-- -------------------------------------------------------------------------
-- Zakat Fund
-- -------------------------------------------------------------------------

(
    'Zakat',
    'ZAKAT',
    'Reserved for annual zakat obligations.',
    TRUE
),

-- -------------------------------------------------------------------------
-- Investment Fund
-- -------------------------------------------------------------------------

(
    'Investment',
    'INVESTMENT',
    'Money allocated for long-term investments.',
    TRUE
)

ON CONFLICT (name)
DO NOTHING;