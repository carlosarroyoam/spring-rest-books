INSERT INTO authors(name, created_at, updated_at) VALUES ('Yuval Noah Harari', '2025-01-01 00:00:00', '2025-01-01 00:00:00');
INSERT INTO authors(name, created_at, updated_at) VALUES ('Itzik Yahav', '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO books(isbn, title, cover_url, price, is_available_online, published_at, created_at, updated_at) VALUES ('978-1-3035-0529-4', 'Homo Deus: A Brief History of Tomorrow', 'https://images.isbndb.com/covers/39/36/9781784703936.jpg', 22.99, 0, '2017-01-01', '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO books(isbn, title, cover_url, price, is_available_online, published_at, created_at, updated_at) VALUES ('978-9-7389-4434-3', 'Sapiens: A Brief History of Humankind', 'https://images.isbndb.com/covers/60/97/9780062316097.jpg', 20.79, 0, '2022-12-01', '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO book_authors(book_id, author_id) VALUES (1, 1);
INSERT INTO book_authors(book_id, author_id) VALUES (1, 2);
INSERT INTO book_authors(book_id, author_id) VALUES (2, 1);

INSERT INTO users(first_name, last_name, email, username, created_at, updated_at) VALUES ('Carlos Alberto', 'Arroyo Martínez', 'carroyom@mail.com', 'carroyom', '2025-01-01 00:00:00', '2025-01-01 00:00:00');
INSERT INTO users(first_name, last_name, email, username, created_at, updated_at) VALUES ('Cathy Stefania', 'Guido Rojas', 'cguidor@mail.com', 'cguidor', '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO carts(user_id, created_at, updated_at) VALUES (1, '2025-01-01 00:00:00', '2025-01-01 00:00:00');
INSERT INTO carts(user_id, created_at, updated_at) VALUES (2, '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO cart_items(book_id, cart_id, quantity, added_at) VALUES (1, 1, 1, '2025-01-01 00:00:00');
INSERT INTO cart_items(book_id, cart_id, quantity, added_at) VALUES (1, 2, 1, '2025-01-01 00:00:00');
INSERT INTO cart_items(book_id, cart_id, quantity, added_at) VALUES (2, 2, 2, '2025-01-01 00:00:00');
