-- Module 11: seed data lives in its own migration so every environment starts the same.
insert into authors (name) values
    ('Joshua Bloch'), ('Robert C. Martin'), ('Martin Kleppmann'), ('Craig Walls'),
    ('Brian Goetz'), ('Eric Evans'), ('Martin Fowler'), ('Eric Freeman'),
    ('Michael T. Nygard'), ('Sam Newman');

insert into books (title, isbn, price, stock, author_id) values
    ('Effective Java', '9780134685991', 45.99, 12, 1),
    ('Clean Code', '9780132350884', 37.50, 8, 2),
    ('Designing Data-Intensive Applications', '9781449373320', 49.99, 15, 3),
    ('Spring in Action', '9781617297571', 54.99, 6, 4),
    ('Java Concurrency in Practice', '9780321349606', 52.00, 2, 5),
    ('Domain-Driven Design', '9780321125217', 61.25, 4, 6),
    ('Refactoring', '9780134757599', 47.80, 9, 7),
    ('Head First Design Patterns', '9781492078005', 44.00, 11, 8),
    ('Release It!', '9781680502398', 41.95, 1, 9),
    ('Building Microservices', '9781492034025', 50.40, 7, 10);
