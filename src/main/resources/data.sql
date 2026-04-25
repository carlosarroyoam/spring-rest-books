INSERT INTO authors(name, bio, status, created_at, updated_at) VALUES 
('Yuval Noah Harari', 'Israeli public intellectual, historian and professor in the Department of History at Hebrew University of Jerusalem. Known for his books on the history of humankind.', 'ACTIVE', '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
('Itzik Yahav', 'Senior software engineer and author specializing in C# and .NET development with over 15 years of experience in enterprise software design.', 'ACTIVE', '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO books(isbn, title, cover_url, price, is_available_online, published_at, status, created_at, updated_at) VALUES
('978-1-3035-0529-4', 'Homo Deus: A Brief History of Tomorrow', 'https://images.isbndb.com/covers/39/36/9781784703936.jpg', 22.99, 0, '2017-01-01', 'ACTIVE', '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
('978-9-7389-4434-3', 'Sapiens: A Brief History of Humankind', 'https://images.isbndb.com/covers/60/97/9780062316097.jpg', 20.79, 0, '2022-12-01', 'ACTIVE', '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO book_authors(book_id, author_id) VALUES 
(1, 1),
(1, 2),
(2, 1);

INSERT INTO customers(first_name, last_name, email, username, status, created_at, updated_at) VALUES
('Carlos Alberto', 'Arroyo Martínez', 'carroyom@mail.com', 'carroyom', 'ACTIVE', '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
('Cathy Stefania', 'Guido Rojas', 'cguidor@mail.com', 'cguidor', 'ACTIVE', '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO carts(customer_id, created_at, updated_at) VALUES
(1, '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
(2, '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO cart_items(book_id, cart_id, quantity, added_at) VALUES
(1, 1, 1, '2025-01-01 00:00:00'),
(1, 2, 1, '2025-01-01 00:00:00'),
(2, 2, 2, '2025-01-01 00:00:00');

INSERT INTO orders(order_number, status, customer_id, subtotal, tax_amount, shipping_amount, total, notes, shipping_address, billing_address, created_at, updated_at) VALUES
('ORD-20250001', 'SHIPPED', 1, 45.98, 7.36, 0.00, 53.34, 'Leave at the front desk', '123 Main Street, Springfield', '123 Main Street, Springfield', '2025-01-02 10:00:00', '2025-01-03 09:30:00'),
('ORD-20250002', 'DELIVERED', 2, 64.57, 10.33, 0.00, 74.90, 'Ring the bell twice', '456 Oak Avenue, Shelbyville', '456 Oak Avenue, Shelbyville', '2025-01-04 11:15:00', '2025-01-06 17:45:00');

INSERT INTO order_items(order_id, book_id, quantity, unit_price, total_price, created_at, updated_at) VALUES
(1, 1, 2, 22.99, 45.98, '2025-01-02 10:00:00', '2025-01-02 10:00:00'),
(2, 1, 1, 22.99, 22.99, '2025-01-04 11:15:00', '2025-01-04 11:15:00'),
(2, 2, 2, 20.79, 41.58, '2025-01-04 11:15:00', '2025-01-04 11:15:00');

INSERT INTO payments(amount, method, status, transaction_id, order_id, created_at, updated_at) VALUES
(53.34, 'CREDIT_CARD', 'COMPLETED', 'PAY-SEED-000001', 1, '2025-01-02 10:00:00', '2025-01-02 10:00:00'),
(74.90, 'PAYPAL', 'COMPLETED', 'PAY-SEED-000002', 2, '2025-01-04 11:15:00', '2025-01-04 11:15:00');

INSERT INTO shipments(attention_name, address, phone, status, order_id, created_at, updated_at) VALUES
('Carlos Alberto Arroyo Martínez', '123 Main Street, Springfield', '5501011234', 'SHIPPED', 1, '2025-01-04 11:15:00', '2025-01-04 11:15:00'),
('Cathy Stefania Guido Rojas', '456 Oak Avenue, Shelbyville', '5501022349', 'DELIVERED', 2, '2025-01-04 11:15:00', '2025-01-04 11:15:00');
