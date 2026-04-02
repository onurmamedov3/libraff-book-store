INSERT INTO companies (name, location, tax_id, telephone) 
VALUES ('Libraff LLC', 'Baku, Azerbaijan', '1234567890', '+994 12 000 00 00');

INSERT INTO stores (name, address, phone, company_id) VALUES 
('Libraff Fountain Square', '9 Aziz Aliyev St, Baku', '+994 12 493 00 00', 1),
('Libraff Park Bulvar', 'Neftchilar Ave, Baku', '+994 12 598 00 01', 1);

-- 2. INSERT BOOKS (Linked to store_id)
INSERT INTO genres (name) VALUES ('Fiction'), ('Dystopian'), ('Technology'),('History/Non-fiction');

INSERT INTO books (name, date_published, publication_amount, purchase_price, sales_price, store_id, genre_id) VALUES
('The Great Gatsby', '1925-04-10',60, 10.50, 15.99, 1, 1), 
('1984', '1949-06-08',35, 8.00, 12.50, 1, 2), 
('Clean Code', '2008-08-01',15, 35.00, 45.00, 2, 3), 
('The Pragmatic Programmer', '1999-10-20',20, 29.99, 39.99, 1, 3), 
('To Kill a Mockingbird', '1960-07-11',50, 9.00, 14.99, 1, 1), 
('Brave New World', '1932-08-01',40, 7.50, 11.99, 2, 2), 
('Refactoring', '1999-07-08',10, 39.99, 54.99, 2, 3), 
('Sapiens', '2011-09-04',30, 18.00, 25.00, 1, 4), 
('The Catcher in the Rye', '1951-07-16',45, 6.50, 10.99, 2, 1); 

-- 3. INSERT AUTHORS 
INSERT INTO authors (name, surname, email) VALUES 
('F. Scott', 'Fitzgerald', 'scott@example.com'), 
('George', 'Orwell', 'george@dystopia.com'), 
('Robert', 'Martin', 'unclebob@clean.com');

-- 4. INSERT INTO JOIN TABLE (The Many-to-Many Bridge)
INSERT INTO book_authors (book_id, author_id) VALUES 
(1, 1), (2, 2), (3, 3), (4, 3); 

INSERT INTO book_stocks (book_id, store_id, quantity, last_updated) VALUES
(1, 1, 50,  CURRENT_TIMESTAMP), 
(2, 1, 30,  CURRENT_TIMESTAMP), 
(3, 2, 15,  CURRENT_TIMESTAMP), 
(4, 1, 20,  CURRENT_TIMESTAMP),
(1, 2, 10, CURRENT_TIMESTAMP), 
(2, 2, 5,  CURRENT_TIMESTAMP);

INSERT INTO positions (name, min_salary, max_salary) VALUES 
('CASHIER', 800.0, 1500.0),
('SALES_REPRESENTATIVE', 900.0, 1500.0),
('CHIEF_SALES_REPRESENTATIVE', 1200.0, 2000.0),
('STORE_MANAGER', 3450.0, 4800.0);

-- ====================================================================
-- JWT & SPRING SECURITY INTEGRATION ZONE
-- ====================================================================

-- 5. INSERT ROLES (These map to Spring Security GrantedAuthorities)
INSERT INTO roles (name) VALUES 
('ROLE_SELL_BOOK'),          -- 1
('ROLE_ADD_BOOK'),           -- 2
('ROLE_RESTOCK_BOOK'),       -- 3
('ROLE_REQUEST_TRANSFER'),   -- 4
('ROLE_APPROVE_TRANSFER'),   -- 5
('ROLE_ADD_DISCOUNT'),        -- 6  
('ROLE_ADD_EMPLOYEE'),       -- 7
('ROLE_GET_EMPLOYEE'),       -- 8
('ROLE_DELETE_EMPLOYEE'),    -- 9
('ROLE_PATCH_EMPLOYEE'),     -- 10
('ROLE_REHIRE_EMPLOYEE'),    -- 11
('ROLE_ADD_GRADE'),          -- 12
('ROLE_GET_GRADE');          -- 13

-- 6. INSERT EMPLOYEES (Now acting as system Users)
-- Note: All passwords are set to 'pass123' using BCrypt hashing.
INSERT INTO employees (FIN, name, surname, password, is_active, email, phone, salary, date_employed, store_id, position_id) VALUES 
('AZ11234', 'Rafiq', 'Şiriyev', '$2a$12$ewpNyWFfE/ibuC3fAldFOuXPpE7bpxThTDf1/yxQOY2o4md9Cn5Ym', TRUE, 'rafiq.shiriyev@store.com', '+994505551111', 2000.00, '2026-03-02', 1, 1),
('AZ11235', 'Nərmin', 'Məmmədova', '$2a$12$ewpNyWFfE/ibuC3fAldFOuXPpE7bpxThTDf1/yxQOY2o4md9Cn5Ym', TRUE, 'nermin.mammadova@store.com', '+994505552222', 3000.00, '2026-02-17', 1, 2),
('AZ11236', 'Əhməd', 'Quliyev', '$2a$12$ewpNyWFfE/ibuC3fAldFOuXPpE7bpxThTDf1/yxQOY2o4md9Cn5Ym', TRUE, 'ahmad.guliyev@store.com', '+994505553333', 4500.00, '2026-02-20', 1, 3),
('AZ99999', 'Huseyn', 'Zulfuqarov', '$2a$12$ewpNyWFfE/ibuC3fAldFOuXPpE7bpxThTDf1/yxQOY2o4md9Cn5Ym', TRUE, 'huseynzulfuqarov@store.com', '+994505559999', 4800.00, '2026-02-10', 1, 4);

-- 7. INSERT EMPLOYEE_ROLES (Replacing the redundant user_roles table)
-- ==========================================
-- Cashier (Rafiq - Employee ID 1)
-- Best Practice: Cashiers only handle point-of-sale operations.
-- ==========================================
INSERT INTO employee_roles (employee_id, role_id) VALUES 
(1, 1); -- ROLE_SELL_BOOK

-- ==========================================
-- Sales Representative (Nərmin - Employee ID 2)
-- Best Practice: Can sell books and request items from other branches for customers.
-- ==========================================
INSERT INTO employee_roles (employee_id, role_id) VALUES 
(2, 1), -- ROLE_SELL_BOOK
(2, 4); -- ROLE_REQUEST_TRANSFER

-- ==========================================
-- Chief Sales Rep / Floor Supervisor (Əhməd - Employee ID 3)
-- Best Practice: Handles sales, transfers, receiving warehouse inventory, and applying daily discounts.
-- ==========================================
INSERT INTO employee_roles (employee_id, role_id) VALUES 
(3, 1), -- ROLE_SELL_BOOK
(3, 3), -- ROLE_RESTOCK_BOOK
(3, 4), -- ROLE_REQUEST_TRANSFER
(3, 6); -- ROLE_ADD_DICOUNT

-- ==========================================
-- Store Manager (Huseyn - Employee ID 4)
-- Best Practice: Full administrative access. Approves transfers, manages catalog, HR duties, and payroll rules.
-- ==========================================
INSERT INTO employee_roles (employee_id, role_id) VALUES 
(4, 1), -- ROLE_SELL_BOOK
(4, 2), -- ROLE_ADD_BOOK
(4, 3), -- ROLE_RESTOCK_BOOK
(4, 4), -- ROLE_REQUEST_TRANSFER
(4, 5), -- ROLE_APPROVE_TRANSFER
(4, 6), -- ROLE_ADD_DICOUNT
(4, 7), -- ROLE_ADD_EMPLOYEE
(4, 8), -- ROLE_GET_EMPLOYEE
(4, 9), -- ROLE_DELETE_EMPLOYEE
(4, 10),-- ROLE_PATCH_EMPLOYEE
(4, 11),-- ROLE_REHIRE_EMPLOYEE
(4, 12),-- ROLE_ADD_GRADE
(4, 13);-- ROLE_GET_GRADE


-- ====================================================================
-- PAYROLL & GRADES ZONE
-- ====================================================================

INSERT INTO grade_structure (bonus_name, bonus_amount, bonus_percentage, min_sales_threshold,bonus_type, target_type, bonus_frequency) VALUES 
('Kassir Aylıq Sabit', 100.0, NULL, 15.0, 'GRADE1' ,'EMPLOYEE', 'MONTHLY'),
('Satış Təmsilçisi Faiz', NULL, 5, 30.0, 'GRADE2' , 'EMPLOYEE', 'MONTHLY'),
('Satış Təmsilçisi Faiz', NULL, 10, 150.0, 'GRADE3' , 'EMPLOYEE', 'MONTHLY'),
('Mağaza İllik Mükafat', 500.0, NULL, 10000.0,'GRADE2', 'STORE', 'ANNUAL'),
('Mağaza Aylıq Mükafat', 75.0, NULL, 60.0,'GRADE1', 'STORE', 'MONTHLY'),
('Mağaza Aylıq Mükafat', NULL, 8, 180.0,'GRADE2', 'STORE', 'MONTHLY'),
('Baş Satış Təmsilçisi Faiz', NULL, 20, 5000.0,'GRADE3' , 'EMPLOYEE', 'MONTHLY');

INSERT INTO grade_position (position_id, bonus_id) VALUES 
(1, 1),
(2, 2),
(3, 2),
(2, 3),
(3, 3);

INSERT INTO grade_store (store_id, bonus_id) VALUES 
(1, 4),
(2, 4),
(1, 5),
(1, 6);