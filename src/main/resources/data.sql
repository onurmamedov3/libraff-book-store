


INSERT INTO companies (name, location, tax_id, telephone) 
VALUES ('Libraff LLC', 'Baku, Azerbaijan', '1234567890', '+994 12 000 00 00');


INSERT INTO stores (name, address, phone, company_id) VALUES 
('Libraff Fountain Square', '9 Aziz Aliyev St, Baku', '+994 12 493 00 00', 1),
('Libraff Park Bulvar', 'Neftchilar Ave, Baku', '+994 12 598 00 01', 1);

-- 2. INSERT BOOKS (Linked to store_id)
INSERT INTO books (name, genre, date_published, purchase_price, sales_price, store_id) VALUES 
('The Great Gatsby', 'Classic', '1925-04-10', 10.50, 15.99, 1),
('1984', 'Dystopian', '1949-06-08', 8.00, 12.50, 1),
('Clean Code', 'Technology', '2008-08-01', 35.00, 45.00, 2),
('The Pragmatic Programmer', 'Technology', '1999-10-20', 29.99, 39.99, 1);

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

