INSERT INTO permissions (name, code, description, created_at, modified_at)
VALUES
    ('Read Payments',   'PERMISSION_PAYMENT_READ',   'Allows reading payment records.',         NOW(), NOW()),
    ('Manage Payments', 'PERMISSION_PAYMENT_MANAGE', 'Allows managing and refunding payments.', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMIN'
ON CONFLICT DO NOTHING;

-- SUPPORT_AGENT gets payment read + manage
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('PERMISSION_PAYMENT_READ', 'PERMISSION_PAYMENT_MANAGE')
WHERE r.code = 'SUPPORT_AGENT'
ON CONFLICT DO NOTHING;
