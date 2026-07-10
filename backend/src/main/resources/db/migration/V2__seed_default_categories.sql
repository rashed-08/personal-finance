-- ============================================================================
-- V2 - Seed Default Categories
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Income Categories
-- ----------------------------------------------------------------------------

INSERT INTO categories (
    name,
    category_type,
    is_system,
    sort_order
)
VALUES
    ('Salary', 'INCOME', TRUE, 1),
    ('Bonus', 'INCOME', TRUE, 2),
    ('Business', 'INCOME', TRUE, 3),
    ('Investment', 'INCOME', TRUE, 4),
    ('Gift', 'INCOME', TRUE, 5),
    ('Cashback', 'INCOME', TRUE, 6),
    ('Refund', 'INCOME', TRUE, 7),
    ('Other Income', 'INCOME', TRUE, 999)
ON CONFLICT (name, category_type)
DO NOTHING;

-- ----------------------------------------------------------------------------
-- Expense Categories
-- ----------------------------------------------------------------------------

INSERT INTO categories (
    name,
    category_type,
    is_system,
    sort_order
)
VALUES
    ('Food', 'EXPENSE', TRUE, 1),
    ('Groceries', 'EXPENSE', TRUE, 2),
    ('Transport', 'EXPENSE', TRUE, 3),
    ('Utilities', 'EXPENSE', TRUE, 4),
    ('Rent', 'EXPENSE', TRUE, 5),
    ('Medical', 'EXPENSE', TRUE, 6),
    ('Education', 'EXPENSE', TRUE, 7),
    ('Shopping', 'EXPENSE', TRUE, 8),
    ('Entertainment', 'EXPENSE', TRUE, 9),
    ('Travel', 'EXPENSE', TRUE, 10),
    ('Family', 'EXPENSE', TRUE, 11),
    ('Donation', 'EXPENSE', TRUE, 12),
    ('Afia', 'EXPENSE', TRUE, 13),
    ('Other Expense', 'EXPENSE', TRUE, 999)
ON CONFLICT (name, category_type)
DO NOTHING;