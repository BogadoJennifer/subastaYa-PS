INSERT INTO wallets (
    available_balance,
    retained_balance,
    total_balance,
    version,
    users_id
)
SELECT
    0,
    0,
    0,
    0,
    u.id
FROM users u
WHERE NOT EXISTS (
    SELECT 1
    FROM wallets w
    WHERE w.users_id = u.id
);