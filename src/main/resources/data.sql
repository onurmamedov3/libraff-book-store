


INSERT INTO companies (name, location, tax_id, telephone) 
VALUES ('Libraff LLC', 'Baku, Azerbaijan', '1234567890', '+994 12 000 00 00');


INSERT INTO stores (name, address, phone, company_id) VALUES 
('Libraff Fountain Square', '9 Aziz Aliyev St, Baku', '+994 12 493 00 00', 1),
('Libraff Park Bulvar', 'Neftchilar Ave, Baku', '+994 12 598 00 01', 1);

-- 2. INSERT BOOKS (Linked to store_id)
INSERT INTO genres (name) VALUES ('Fiction'), ('Dystopian'), ('Technology');


INSERT INTO books (name, date_published, purchase_price, sales_price, store_id, genre_id) VALUES
('The Great Gatsby',          '1925-04-10', 10.50, 15.99, 1, 1),  -- Fiction
('1984',                      '1949-06-08',  8.00, 12.50, 1, 2),  -- Dystopian
('Clean Code',                '2008-08-01', 35.00, 45.00, 2, 3),  -- Technology
('The Pragmatic Programmer',  '1999-10-20', 29.99, 39.99, 1, 3);  -- Technology

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