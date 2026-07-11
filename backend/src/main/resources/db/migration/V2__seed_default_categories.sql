-- ============================================================================
-- V2 - Seed Default Categories
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Income Categories
-- ----------------------------------------------------------------------------

INSERT INTO categories (
    name,
    category_type,
    is_system
)
VALUES
    ('Salary', 'INCOME', TRUE),
    ('Bonus', 'INCOME', TRUE),
    ('Business', 'INCOME', TRUE),
    ('Investment', 'INCOME', TRUE),
    ('Gift', 'INCOME', TRUE),
    ('Cashback', 'INCOME', TRUE),
    ('Refund', 'INCOME', TRUE),
    ('Other Income', 'INCOME', TRUE)
ON CONFLICT (name, category_type)
DO NOTHING;

-- ----------------------------------------------------------------------------
-- Expense Categories
-- ----------------------------------------------------------------------------

INSERT INTO categories (
    name,
    category_type,
    is_system
)
VALUES
    ('Food', 'EXPENSE', TRUE),
    ('Groceries', 'EXPENSE', TRUE),
    ('Transport', 'EXPENSE', TRUE),
    ('Utilities', 'EXPENSE', TRUE),
    ('Rent', 'EXPENSE', TRUE),
    ('Medical', 'EXPENSE', TRUE),
    ('Education', 'EXPENSE', TRUE),
    ('Shopping', 'EXPENSE', TRUE),
    ('Entertainment', 'EXPENSE', TRUE),
    ('Travel', 'EXPENSE', TRUE),
    ('Family', 'EXPENSE', TRUE),
    ('Donation', 'EXPENSE', TRUE),
    ('Afia', 'EXPENSE', TRUE),
    ('Other Expense', 'EXPENSE', TRUE)
ON CONFLICT (name, category_type)
DO NOTHING;