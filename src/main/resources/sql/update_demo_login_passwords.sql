USE LIMS_DB;
GO

-- Update the five seeded demo accounts without dropping or recreating tables.
-- Password for each account: 123456
UPDATE USERS
SET PasswordHash = '$2a$10$sZR7mHLsop2ipiE2PZzGouC/VnYLRi9bKzgJCw8pl2TuHgEvdp/Je'
WHERE Username IN ('admin', 'manager', 'warehouse', 'sales', 'purchasing');
GO

SELECT Username, Status
FROM USERS
WHERE Username IN ('admin', 'manager', 'warehouse', 'sales', 'purchasing')
ORDER BY UserID;
GO
