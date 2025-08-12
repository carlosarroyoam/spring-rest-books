INSERT INTO authors(name, created_at, updated_at) VALUES ('Yuval Noah Harari', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO authors(name, created_at, updated_at) VALUES ('Itzik Yahav', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO books(isbn, title, cover_url, price, is_available_online, published_at, created_at, updated_at) 
VALUES ('978-1-3035-0529-4', 'Homo Deus: A Brief History of Tomorrow', 'https://images.isbndb.com/covers/39/36/9781784703936.jpg', 22.99, 0, '2017-01-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO books(isbn, title, cover_url, price, is_available_online, published_at, created_at, updated_at) 
VALUES ('978-9-7389-4434-3', 'Sapiens: A Brief History of Humankind', 'https://images.isbndb.com/covers/60/97/9780062316097.jpg', 20.79, 0, '2022-12-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO book_authors(book_id, author_id) VALUES (1, 1);
INSERT INTO book_authors(book_id, author_id) VALUES (1, 2);
INSERT INTO book_authors(book_id, author_id) VALUES (2, 1);

INSERT INTO roles(description, title) VALUES ('Role for admins users', 'App//Admin');
INSERT INTO roles(description, title) VALUES ('Role for customer users', 'App//Customer');

INSERT INTO users(name, age, email, username, role_id, is_active, created_at, updated_at)
VALUES ('Carlos Alberto Arroyo Martínez', 28, 'carroyom@mail.com', 'carroyom', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users(name, age, email, username, role_id, is_active, created_at, updated_at)
VALUES ('Cathy Stefania Guido Rojas', 28, 'cguidor@mail.com', 'cguidor', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO shopping_carts(user_id, created_at, updated_at) VALUES (2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO cart_items(book_id, shopping_cart_id, quantity, added_at) VALUES (1, 1, 1, CURRENT_TIMESTAMP);
INSERT INTO cart_items(book_id, shopping_cart_id, quantity, added_at) VALUES (2, 1, 2, CURRENT_TIMESTAMP);
