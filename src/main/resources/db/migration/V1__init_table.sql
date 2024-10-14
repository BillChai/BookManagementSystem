CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY, -- Auto-incrementing ID for books
    title VARCHAR(255),
    price DECIMAL CHECK (price >= 0),
    published_status BOOLEAN DEFAULT FALSE
);

CREATE TABLE authors (
    id BIGSERIAL PRIMARY KEY, -- Auto-incrementing ID for authors
    name VARCHAR(255),
    birthdate DATE CHECK (birthdate < CURRENT_DATE)
);

CREATE TABLE book_author (
    book_id BIGINT,
    author_id BIGINT,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (author_id) REFERENCES authors(id)
);
