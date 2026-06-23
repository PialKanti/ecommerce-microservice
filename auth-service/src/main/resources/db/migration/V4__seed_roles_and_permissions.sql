INSERT INTO roles (name, code, description, created_at, modified_at)
VALUES
    ('Administrator',    'ADMIN',              'Full platform administrator with all permissions.',            NOW(), NOW()),
    ('Customer',         'CUSTOMER',           'Default shopper role for registered customers.',               NOW(), NOW()),
    ('Product Manager',  'PRODUCT_MANAGER',    'Manages product and category catalog operations.',             NOW(), NOW()),
    ('Inventory Manager','INVENTORY_MANAGER',  'Manages product inventory and stock movements.',               NOW(), NOW()),
    ('Support Agent',    'SUPPORT_AGENT',      'Supports customer order lookup and cancellation workflows.',   NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO permissions (name, code, description, created_at, modified_at)
VALUES
    ('Read Products',      'PERMISSION_PRODUCT_READ',      'Allows reading product catalog data.',                              NOW(), NOW()),
    ('Create Products',    'PERMISSION_PRODUCT_CREATE',    'Allows creating product catalog records.',                          NOW(), NOW()),
    ('Update Products',    'PERMISSION_PRODUCT_UPDATE',    'Allows updating product catalog records.',                          NOW(), NOW()),
    ('Delete Products',    'PERMISSION_PRODUCT_DELETE',    'Allows deleting product catalog records.',                          NOW(), NOW()),
    ('Read Categories',    'PERMISSION_CATEGORY_READ',     'Allows reading product category data.',                             NOW(), NOW()),
    ('Create Categories',  'PERMISSION_CATEGORY_CREATE',   'Allows creating product category records.',                         NOW(), NOW()),
    ('Update Categories',  'PERMISSION_CATEGORY_UPDATE',   'Allows updating product category records.',                         NOW(), NOW()),
    ('Delete Categories',  'PERMISSION_CATEGORY_DELETE',   'Allows deleting product category records.',                         NOW(), NOW()),
    ('Read Inventory',     'PERMISSION_INVENTORY_READ',    'Allows reading inventory records.',                                 NOW(), NOW()),
    ('Manage Inventory',   'PERMISSION_INVENTORY_MANAGE',  'Allows inventory creation, adjustment, reservation, and release.',  NOW(), NOW()),
    ('Read Orders',        'PERMISSION_ORDER_READ',        'Allows reading customer order records.',                            NOW(), NOW()),
    ('Update Orders',      'PERMISSION_ORDER_UPDATE',      'Allows updating customer order records.',                           NOW(), NOW()),
    ('Cancel Orders',      'PERMISSION_ORDER_CANCEL',      'Allows cancelling eligible customer orders.',                       NOW(), NOW()),
    ('Read Users',         'PERMISSION_USER_READ',         'Allows reading user account records.',                              NOW(), NOW()),
    ('Manage Users',       'PERMISSION_USER_MANAGE',       'Allows managing user account records.',                             NOW(), NOW()),
    ('Read Roles',         'PERMISSION_ROLE_READ',         'Allows reading RBAC roles.',                                        NOW(), NOW()),
    ('Create Roles',       'PERMISSION_ROLE_CREATE',       'Allows creating RBAC roles.',                                       NOW(), NOW()),
    ('Update Roles',       'PERMISSION_ROLE_UPDATE',       'Allows updating RBAC roles.',                                       NOW(), NOW()),
    ('Delete Roles',       'PERMISSION_ROLE_DELETE',       'Allows deleting RBAC roles.',                                       NOW(), NOW()),
    ('Read Permissions',   'PERMISSION_READ',              'Allows reading RBAC permissions.',                                  NOW(), NOW()),
    ('Create Permissions', 'PERMISSION_CREATE',            'Allows creating RBAC permissions.',                                 NOW(), NOW()),
    ('Update Permissions', 'PERMISSION_UPDATE',            'Allows updating RBAC permissions.',                                 NOW(), NOW()),
    ('Delete Permissions', 'PERMISSION_DELETE',            'Allows deleting RBAC permissions.',                                 NOW(), NOW()),
    ('Assign Roles',       'PERMISSION_ROLE_ASSIGN',       'Allows assigning roles to users.',                                  NOW(), NOW()),
    ('Assign Permissions', 'PERMISSION_ASSIGN',            'Allows assigning permissions to roles.',                            NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMIN'
ON CONFLICT DO NOTHING;

-- PRODUCT_MANAGER: product + category CRUD
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'PERMISSION_PRODUCT_READ',
    'PERMISSION_PRODUCT_CREATE',
    'PERMISSION_PRODUCT_UPDATE',
    'PERMISSION_PRODUCT_DELETE',
    'PERMISSION_CATEGORY_READ',
    'PERMISSION_CATEGORY_CREATE',
    'PERMISSION_CATEGORY_UPDATE',
    'PERMISSION_CATEGORY_DELETE'
)
WHERE r.code = 'PRODUCT_MANAGER'
ON CONFLICT DO NOTHING;

-- INVENTORY_MANAGER: inventory read + manage
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('PERMISSION_INVENTORY_READ', 'PERMISSION_INVENTORY_MANAGE')
WHERE r.code = 'INVENTORY_MANAGER'
ON CONFLICT DO NOTHING;

-- SUPPORT_AGENT: order read + cancel
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('PERMISSION_ORDER_READ', 'PERMISSION_ORDER_CANCEL')
WHERE r.code = 'SUPPORT_AGENT'
ON CONFLICT DO NOTHING;

-- Demo admin user (password: StrongPass123)
INSERT INTO users (
    first_name, last_name, username, email, password,
    phone_number, is_active, created_at, modified_at, created_by, modified_by
)
VALUES (
    'Demo', 'Admin', 'admin', 'admin@example.com',
    '$2a$10$KVRXQyQb86Aza2PV.4K9mOPxo4waXjUk5mGTsy6QtvVDdCzy6ykk6',
    NULL, TRUE, NOW(), NOW(), NULL, NULL
)
ON CONFLICT DO NOTHING;

-- Assign ADMIN role to demo admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.code = 'ADMIN'
WHERE u.username = 'admin'
ON CONFLICT DO NOTHING;

-- Assign CUSTOMER role to all non-admin users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.code = 'CUSTOMER'
WHERE u.username <> 'admin'
ON CONFLICT DO NOTHING;
