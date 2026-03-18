


INSERT INTO companies (name, location, tax_id, telephone) 
VALUES ('Libraff LLC', 'Baku, Azerbaijan', '1234567890', '+994 12 000 00 00');


INSERT INTO stores (name, address, phone, company_id) VALUES 
('Libraff Fountain Square', '9 Aziz Aliyev St, Baku', '+994 12 493 00 00', 1),
('Libraff Park Bulvar', 'Neftchilar Ave, Baku', '+994 12 598 00 01', 1);

-- 2. INSERT BOOKS (Linked to store_id)
INSERT INTO genres (name) VALUES ('Fiction'), ('Dystopian'), ('Technology'),('History/Non-fiction');


INSERT INTO books (name, date_published, publication_amount, purchase_price, sales_price, store_id, genre_id) VALUES
('The Great Gatsby', '1925-04-10',60, 10.50, 15.99, 1, 1), -- Fiction
('1984', '1949-06-08',35, 8.00, 12.50, 1, 2), -- Dystopian
('Clean Code', '2008-08-01',15, 35.00, 45.00, 2, 3), -- Technology
('The Pragmatic Programmer', '1999-10-20',20, 29.99, 39.99, 1, 3), -- Technology
('To Kill a Mockingbird', '1960-07-11',50, 9.00, 14.99, 1, 1), -- Fiction
('Brave New World', '1932-08-01',40, 7.50, 11.99, 2, 2), -- Dystopian
('Refactoring', '1999-07-08',10, 39.99, 54.99, 2, 3), -- Technology
('Sapiens', '2011-09-04',30, 18.00, 25.00, 1, 4), -- History/Non-fiction
('The Catcher in the Rye', '1951-07-16',45, 6.50, 10.99, 2, 1); -- Fiction

-- 3. INSERT AUTHORS (No book_id here!)
INSERT INTO authors (name, surname, email) VALUES 
('F. Scott', 'Fitzgerald', 'scott@example.com'), 
('George', 'Orwell', 'george@dystopia.com'), 
('Robert', 'Martin', 'unclebob@clean.com');

-- 4. INSERT INTO JOIN TABLE (The Many-to-Many Bridge)
-- This links the books to the authors
INSERT INTO book_authors (book_id, author_id) VALUES 
(1, 1), -- The Great Gatsby (Book 1) linked to F. Scott Fitzgerald (Author 1)
(2, 2), -- 1984 (Book 2) linked to George Orwell (Author 2)
(3, 3),
(4, 3); -- Clean Code (Book 3) linked to Robert Martin (Author 3)

INSERT INTO book_stocks (book_id, store_id, quantity, last_updated) VALUES
(1, 1, 50,  CURRENT_TIMESTAMP), -- The Great Gatsby in Libraff Fountain Square
(2, 1, 30,  CURRENT_TIMESTAMP), -- 1984 in Libraff Fountain Square
(3, 2, 15,  CURRENT_TIMESTAMP), -- Clean Code in Libraff Park Bulvar
(4, 1, 20,  CURRENT_TIMESTAMP),
(1, 2, 10, CURRENT_TIMESTAMP), 
(2, 2, 5,  CURRENT_TIMESTAMP);

INSERT INTO positions (name, min_salary, max_salary) VALUES 
('CASHIER', 800.0, 1500.0),
('SALES_REPRESENTATIVE', 900.0, 1500.0),
('CHIEF_SALES_REPRESENTATIVE', 1200.0, 2000.0),
('STORE_MANAGER', 3450.0, 4800.0);


INSERT INTO employees (FIN, name, surname, password, is_active, email, phone, salary, date_employed, store_id, position_id) VALUES 
('AZ11234', 'Rafiq', 'Şiriyev', 'pass123', TRUE, 'rafiq.shiriyev@store.com', '+994505551111', 2000.00, '2026-03-02', 1, 1),
('AZ11235', 'Nərmin', 'Məmmədova', 'pass456', TRUE, 'nermin.mammadova@store.com', '+994505552222', 3000.00, '2026-02-17', 1, 2),
('AZ11236', 'Əhməd', 'Quliyev', 'pass789', TRUE, 'ahmad.guliyev@store.com', '+994505553333', 4500.00, '2026-02-20', 1, 3),
('AZ99999', 'Huseyn', 'Zulfuqarov', 'pass789', TRUE, 'huseynzulfuqarov@store.com', '+994505559999', 4800.00, '2026-02-10', 1, 4);


INSERT INTO grade_structure (bonus_name, bonus_amount, bonus_percentage, min_sales_threshold,bonus_type, target_type, bonus_frequency) VALUES 
-- 1. Kassirlər üçün aylıq 100 AZN sabit bonus (Hədəf: 2000 AZN satış)
('Kassir Aylıq Sabit', 100.0, NULL, 15.0, 'GRADE1' ,'EMPLOYEE', 'MONTHLY'),
-- 2. Satış təmsilçiləri üçün aylıq 5% bonus (Hədəf: 5000 AZN satış)
('Satış Təmsilçisi Faiz', NULL, 5, 30.0, 'GRADE2' , 'EMPLOYEE', 'MONTHLY'),
('Satış Təmsilçisi Faiz', NULL, 10, 150.0, 'GRADE3' , 'EMPLOYEE', 'MONTHLY'),
-- 3. Mağaza hədəfi keçəndə illik 500 AZN bonus (Hədəf: 100,000 AZN satış)
('Mağaza İllik Mükafat', 500.0, NULL, 10000.0,'GRADE2', 'STORE', 'ANNUAL'),

('Mağaza Aylıq Mükafat', 75.0, NULL, 60.0,'GRADE1', 'STORE', 'MONTHLY'),

('Mağaza Aylıq Mükafat', NULL, 8, 180.0,'GRADE2', 'STORE', 'MONTHLY'),

('Baş Satış Təmsilçisi Faiz', NULL, 20, 5000.0,'GRADE3' , 'EMPLOYEE', 'MONTHLY');

-- "Kassir Aylıq Sabit" (bonus_id=1) qaydasını Kassir vəzifəsinə (position_id=1) bağlayırıq
INSERT INTO grade_position (position_id, bonus_id) VALUES (1, 1);
-- "Satış Təmsilçisi Faiz" (bonus_id=2) qaydasını Satış Təmsilçisi vəzifəsinə (position_id=2) bağlayırıq
INSERT INTO grade_position (position_id, bonus_id) VALUES (2, 2);
INSERT INTO grade_position (position_id, bonus_id) VALUES (3, 2);
INSERT INTO grade_position (position_id, bonus_id) VALUES (2, 3);
INSERT INTO grade_position (position_id, bonus_id) VALUES (3, 3);
-- İllik bonusu (bonus_id=3) hər iki mağaza üçün aktiv edirik
INSERT INTO grade_store (store_id, bonus_id) VALUES 
(1, 4),
(2, 4),
(1, 5),
(1, 6);
--(1, 4),
--(2, 4);









