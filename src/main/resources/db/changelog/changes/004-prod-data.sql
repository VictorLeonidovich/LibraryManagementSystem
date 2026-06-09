--liquibase formatted sql

--changeset victor:4-prod-dictionaries context:prod
--comment: Заполнение справочников авторов, категорий и издательств
INSERT INTO categories (name) VALUES
                                  ('Фантастика'), ('Детектив'), ('Классическая литература'), ('Фэнтези'), ('Научпоп'), ('Психология')
    ON CONFLICT (name) DO NOTHING;

INSERT INTO publishers (name) VALUES
                                  ('Эксмо'), ('АСТ'), ('Азбука'), ('МИФ'), ('Альпина Паблишер')
    ON CONFLICT (name) DO NOTHING;

INSERT INTO authors (name, description) VALUES
                                            ('Михаил Булгаков', 'Выдающийся советский писатель, драматург и театральный режиссер.'),
                                            ('Федор Достоевский', 'Один из самых значительных и известных в мире русских писателей и мыслителей.'),
                                            ('Александр Пушкин', 'Великий русский поэт, прозаик, драматург, основоположник современного русского языка.'),
                                            ('Лев Толстой', 'Один из величайших писателей-романистов мира, мыслитель и просветитель.'),
                                            ('Аркадий и Борис Стругацкие', 'Культовые советские писатели-фантасты, классики современной научной фантастики.'),
                                            ('Антон Чехов', 'Выдающийся русский писатель, прозаик, драматург, классик мировой литературы.'),
                                            ('Илья Ильф и Евгений Петров', 'Знаменитый советский творческий тандем писателей-сатириков.'),
                                            ('Николай Гоголь', 'Великий русский прозаик, драматург, поэт, критик и публицист.'),
                                            ('Евгений Замятин', 'Известный советский писатель, публицист и литературный критик, автор антиутопий.'),
                                            ('Александр Грибоедов', 'Русский дипломат, поэт, драматург, пианист и композитор.')
    ON CONFLICT (name) DO NOTHING;


--changeset victor:5-prod-books-part1 context:prod
--comment: Книги 1-10 с простановкой связей через подзапросы
-- Книга 1
INSERT INTO books (name, isbn, description) VALUES ('Мастер и Маргарита', '978-5-699-90422-8', 'Культовый роман Михаила Булгакова, сочетающий мистику, сатиру и философию.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-90422-8' AND a.name = 'Михаил Булгаков';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-90422-8' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-90422-8' AND p.name = 'Эксмо';

-- Книга 2
INSERT INTO books (name, isbn, description) VALUES ('Преступление и наказание', '978-5-17-090622-2', 'Глубокий психологический роман Федора Достоевского о цене человеческой жизни.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-090622-2' AND a.name = 'Федор Достоевский';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-090622-2' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-090622-2' AND p.name = 'АСТ';

-- Книга 3
INSERT INTO books (name, isbn, description) VALUES ('Евгений Онегин', '978-5-389-04423-1', 'Энциклопедия русской жизни в стихах от Александра Сергеевича Пушкина.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-04423-1' AND a.name = 'Александр Пушкин';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-04423-1' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-04423-1' AND p.name = 'Азбука';

-- Книга 4
INSERT INTO books (name, isbn, description) VALUES ('Война и мир', '978-5-699-12345-6', 'Масштабная эпопея Льва Толстого, описывающая русское общество в эпоху войн с Наполеоном.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-12345-6' AND a.name = 'Лев Толстой';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-12345-6' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-12345-6' AND p.name = 'Эксмо';

-- Книга 5
INSERT INTO books (name, isbn, description) VALUES ('Пикник на обочине', '978-5-17-088899-3', 'Фантастическая повесть братьев Стругацких о Зоне посещения и сталкерах.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-088899-3' AND a.name = 'Аркадий и Борис Стругацкие';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-088899-3' AND c.name = 'Фантастика';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-088899-3' AND p.name = 'АСТ';

-- Книга 6
INSERT INTO books (name, isbn, description) VALUES ('Повести и рассказы', '978-5-389-11122-2', 'Сборник лучших юмористических и драматических рассказов Антона Чехова.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-11122-2' AND a.name = 'Антон Чехов';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-11122-2' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-11122-2' AND p.name = 'Азбука';

-- Книга 7
INSERT INTO books (name, isbn, description) VALUES ('Двенадцать стульев', '978-5-699-54321-0', 'Блистательный сатирический роман об охоте за сокровищами мадам Петуховой.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-54321-0' AND a.name = 'Илья Ильф и Евгений Петров';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-54321-0' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-54321-0' AND p.name = 'Эксмо';

-- Книга 8
INSERT INTO books (name, isbn, description) VALUES ('Мертвые души', '978-5-17-077755-4', 'Поэма Николая Гоголя, открывающая галерею бессмертных типов русских помещиков.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-077755-4' AND a.name = 'Николай Гоголь';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-077755-4' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-077755-4' AND p.name = 'АСТ';

-- Книга 9
INSERT INTO books (name, isbn, description) VALUES ('Мы', '978-5-389-99988-7', 'Знаменитая антиутопия Евгения Замятина, заложившая основы жанра в мировой литературе.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-99988-7' AND a.name = 'Евгений Замятин';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-99988-7' AND c.name = 'Фантастика';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-99988-7' AND p.name = 'Азбука';

-- Книга 10
INSERT INTO books (name, isbn, description) VALUES ('Горе от ума', '978-5-699-33322-1', 'Бессмертная комедия в стихах, разобранная на цитаты и пословицы.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-33322-1' AND a.name = 'Александр Грибоедов';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-33322-1' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-33322-1' AND p.name = 'Эксмо';

--changeset victor:6-prod-authors-part2 context:prod
--comment: Добавление новых авторов для книг 11-20
INSERT INTO authors (name, description) VALUES
                                            ('Иван Тургенев', 'Знаменитый русский писатель-реалист, поэт, публицист и драматург.'),
                                            ('Михаил Лермонтов', 'Великий русский поэт, прозаик, драматург и художник.'),
                                            ('Артур Конан Дойл', 'Всемирно известный английский писатель, создатель Шерлока Холмса.'),
                                            ('Джордж Оруэлл', 'Британский писатель и публицист, автор культовых антиутопий.')
    ON CONFLICT (name) DO NOTHING;


--changeset victor:7-prod-books-part2 context:prod
--comment: Книги 11-20
-- Книга 11
INSERT INTO books (name, isbn, description) VALUES ('Отцы и дети', '978-5-17-094111-7', 'Знаменитый роман Ивана Тургенева о конфликте поколений и нигилизме.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-094111-7' AND a.name = 'Иван Тургенев';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-094111-7' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-094111-7' AND p.name = 'АСТ';

-- Книга 12
INSERT INTO books (name, isbn, description) VALUES ('Герой нашего времени', '978-5-389-02234-5', 'Первый лирико-психологический роман в русской литературе, написанный Михаилом Лермонтовым.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-02234-5' AND a.name = 'Михаил Лермонтов';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-02234-5' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-02234-5' AND p.name = 'Азбука';

-- Книга 13
INSERT INTO books (name, isbn, description) VALUES ('Приключения Шерлока Холмса', '978-5-699-88112-0', 'Сборник детективных рассказов о гениальном сыщике и его друге докторе Ватсоне.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-88112-0' AND a.name = 'Артур Конан Дойл';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-88112-0' AND c.name = 'Детектив';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-88112-0' AND p.name = 'Эксмо';

-- Книга 14
INSERT INTO books (name, isbn, description) VALUES ('1984', '978-5-17-080112-4', 'Главная антиутопия XX века о тоталитарном обществе, Большом Брате и контроле над мыслями.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-080112-4' AND a.name = 'Джордж Оруэлл';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-080112-4' AND c.name = 'Фантастика';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-080112-4' AND p.name = 'АСТ';

-- Книга 15
INSERT INTO books (name, isbn, description) VALUES ('Собачье сердце', '978-5-699-44552-1', 'Сатирическая повесть Михаила Булгакова об удивительном эксперименте профессора Преображенского.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-44552-1' AND a.name = 'Михаил Булгаков';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-44552-1' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-44552-1' AND p.name = 'Эксмо';

-- Книга 16
INSERT INTO books (name, isbn, description) VALUES ('Трудно быть богом', '978-5-17-089912-2', 'Фантастическая повесть братьев Стругацких об этическом выборе землянина на отсталой планете.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-089912-2' AND a.name = 'Аркадий и Борис Стругацкие';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-089912-2' AND c.name = 'Фантастика';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-089912-2' AND p.name = 'АСТ';

-- Книга 17
INSERT INTO books (name, isbn, description) VALUES ('Понедельник начинается в субботу', '978-5-17-077123-9', 'Фантастическая юмористическая повесть Стругацких о буднях сотрудников института чародейства НИИЧАВО.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-077123-9' AND a.name = 'Аркадий и Борис Стругацкие';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-077123-9' AND c.name = 'Фантастика';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-077123-9' AND p.name = 'АСТ';

-- Книга 18
INSERT INTO books (name, isbn, description) VALUES ('Золотой теленок', '978-5-699-11223-4', 'Продолжение приключений великого комбинатора Остапа Бендера, охотящегося за миллионом Корейко.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-11223-4' AND a.name = 'Илья Ильф и Евгений Петров';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-11223-4' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-11223-4' AND p.name = 'Эксмо';

-- Книга 19
INSERT INTO books (name, isbn, description) VALUES ('Братья Карамазовы', '978-5-389-01145-6', 'Итоговый роман Федора Достоевского, затрагивающий глубокие вопросы веры, морали и человеческой души.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-01145-6' AND a.name = 'Федор Достоевский';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-01145-6' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-01145-6' AND p.name = 'Азбука';

-- Книга 20
INSERT INTO books (name, isbn, description) VALUES ('Записки о Шерлоке Холмсе', '978-5-699-77441-2', 'Еще один великолепный цикл историй о расследованиях на Бейкер-стрит.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-77441-2' AND a.name = 'Артур Конан Дойл';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-77441-2' AND c.name = 'Детектив';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-77441-2' AND p.name = 'Эксмо';

--changeset victor:8-prod-authors-part3 context:prod
--comment: Добавление авторов для книг 21-30
INSERT INTO authors (name, description) VALUES
                                            ('Джон Р. Р. Толкин', 'Английский писатель, лингвист и поэт, создатель классического высокого фэнтези.'),
                                            ('Дейл Карнеги', 'Американский педагог, лектор, писатель, один из создателей теории общения.'),
                                            ('Эрих Мария Ремарк', 'Один из наиболее известных и читаемых немецких писателей двадцатого века.'),
                                            ('Рэй Брэдбери', 'Выдающийся американский писатель-фантаст, классик мировой литературы.')
    ON CONFLICT (name) DO NOTHING;


--changeset victor:9-prod-books-part3 context:prod
--comment: Книги 21-30
-- Книга 21
INSERT INTO books (name, isbn, description) VALUES ('Хоббит, или Туда и обратно', '978-5-17-091234-7', 'Повесть Джона Р. Р. Толкина, положившая начало грандиозному миру Средиземья.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-091234-7' AND a.name = 'Джон Р. Р. Толкин';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-091234-7' AND c.name = 'Фэнтези';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-091234-7' AND p.name = 'АСТ';

-- Книга 22
INSERT INTO books (name, isbn, description) VALUES ('Братство Кольца', '978-5-17-084321-4', 'Первая часть великой трилогии «Властелин колец» о путешествии Фродо Бэггинса.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-084321-4' AND a.name = 'Джон Р. Р. Толкин';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-084321-4' AND c.name = 'Фэнтези';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-084321-4' AND p.name = 'АСТ';

-- Книга 23 (Название укорочено до 46 символов, чтобы пройти валидацию VARCHAR(50))
INSERT INTO books (name, isbn, description) VALUES ('Как завоевывать друзей и влиять на людей', '978-5-17-112233-1', 'Культовая книга Дейла Карнеги по психологии общения и выстраиванию отношений.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-112233-1' AND a.name = 'Дейл Карнеги';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-112233-1' AND c.name = 'Психология';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-112233-1' AND p.name = 'АСТ';

-- Книга 24
INSERT INTO books (name, isbn, description) VALUES ('Три товарища', '978-5-389-05566-1', 'Шедевр Эриха Марии Ремарка о трагической любви и настоящей мужской дружбе.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-05566-1' AND a.name = 'Эрих Мария Ремарк';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-05566-1' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-05566-1' AND p.name = 'Азбука';

-- Книга 25
INSERT INTO books (name, isbn, description) VALUES ('На Западном фронте без перемен', '978-5-389-01239-0', 'Антивоенный роман Ремарка, описывающий ужасы Первой мировой войны глазами солдата.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-01239-0' AND a.name = 'Эрих Мария Ремарк';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-01239-0' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-01239-0' AND p.name = 'Азбука';

-- Книга 26
INSERT INTO books (name, isbn, description) VALUES ('451 градус по Фаренгейту', '978-5-699-98765-2', 'Антиутопия Рэя Брэдбери о поглощенном медиа-культурой обществе, в котором сжигают книги.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-98765-2' AND a.name = 'Рэй Брэдбери';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-98765-2' AND c.name = 'Фантастика';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-98765-2' AND p.name = 'Эксмо';

-- Книга 27
INSERT INTO books (name, isbn, description) VALUES ('Марсианские хроники', '978-5-699-55667-1', 'Цикл удивительных новелл Рэя Брэдбери, посвященный освоению человеком Красной планеты.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-55667-1' AND a.name = 'Рэй Брэдбери';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-55667-1' AND c.name = 'Фантастика';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-55667-1' AND p.name = 'Эксмо';

-- Книга 28
INSERT INTO books (name, isbn, description) VALUES ('Руслан и Людмила', '978-5-389-44556-9', 'Сказочная и романтическая поэма Александра Пушкина с легендарным Лукоморьем.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-44556-9' AND a.name = 'Александр Пушкин';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-44556-9' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-44556-9' AND p.name = 'Азбука';

-- Книга 29
INSERT INTO books (name, isbn, description) VALUES ('Анна Каренина', '978-5-699-00112-2', 'Драматический шедевр Льва Толстого о всепоглощающей страсти и разрушении устоев общества.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-00112-2' AND a.name = 'Лев Толстой';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-00112-2' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-00112-2' AND p.name = 'Эксмо';

-- Книга 30
INSERT INTO books (name, isbn, description) VALUES ('Тарас Бульба', '978-5-17-099441-0', 'Историческая повесть Николая Гоголя о героическом казачестве и драматическом выборе.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-099441-0' AND a.name = 'Николай Гоголь';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-099441-0' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-099441-0' AND p.name = 'АСТ';

--changeset victor:10-prod-authors-part4 context:prod
--comment: Добавление авторов для книг 31-40
INSERT INTO authors (name, description) VALUES
                                            ('Стивен Хокинг', 'Всемирно известный английский физик-теоретик, космолог и популяризатор науки.'),
                                            ('Карл Саган', 'Выдающийся американский астроном, астрофизик и выдающийся популяризатор науки.'),
                                            ('Дэниел Канеман', 'Известный израильско-американский психолог, лауреат Нобелевской премии по экономике.'),
                                            ('Михаил Шолохов', 'Знаменитый советский писатель, журналист, лауреат Нобелевской премии по литературе.')
    ON CONFLICT (name) DO NOTHING;


--changeset victor:11-prod-books-part4 context:prod
--comment: Книги 31-40
-- Книга 31
INSERT INTO books (name, isbn, description) VALUES ('Краткая история времени', '978-5-17-100223-1', 'Главная научно-популярная книга Стивена Хокинга о происхождении Вселенной и черных дырах.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-100223-1' AND a.name = 'Стивен Хокинг';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-100223-1' AND c.name = 'Научпоп';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-100223-1' AND p.name = 'АСТ';

-- Книга 32
INSERT INTO books (name, isbn, description) VALUES ('Космос', '978-5-91671-555-2', 'Культовая книга Карла Сагана об эволюции Вселенной, науки и человеческой цивилизации.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-91671-555-2' AND a.name = 'Карл Саган';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-91671-555-2' AND c.name = 'Научпоп';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-91671-555-2' AND p.name = 'Альпина Паблишер';

-- Книга 33
INSERT INTO books (name, isbn, description) VALUES ('Думай медленно... решай быстро', '978-5-17-080004-2', 'Революционное исследование Дэниела Канемана о двух системах, управляющих нашим мышлением.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-080004-2' AND a.name = 'Дэниел Канеман';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-080004-2' AND c.name = 'Психология';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-080004-2' AND p.name = 'АСТ';

-- Книга 34
INSERT INTO books (name, isbn, description) VALUES ('Тихий Дон', '978-5-699-04123-5', 'Масштабный роман-эпопея Михаила Шолохова о судьбе донского казачества в годы революции.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-04123-5' AND a.name = 'Михаил Шолохов';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-04123-5' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-04123-5' AND p.name = 'Эксмо';

-- Книга 35
INSERT INTO books (name, isbn, description) VALUES ('Двенадцать стульев (подарочное)', '978-5-9614-1234-9', 'Иллюстрированное издание бессмертной советской сатиры с комментариями.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-9614-1234-9' AND a.name = 'Илья Ильф и Евгений Петров';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-9614-1234-9' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-9614-1234-9' AND p.name = 'Альпина Паблишер';

-- Книга 36
INSERT INTO books (name, isbn, description) VALUES ('Две башни', '978-5-17-084322-1', 'Вторая часть легендарного «Властелина колец», описывающая раскол Братства.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-084322-1' AND a.name = 'Джон Р. Р. Толкин';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-084322-1' AND c.name = 'Фэнтези';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-084322-1' AND p.name = 'АСТ';

-- Книга 37
INSERT INTO books (name, isbn, description) VALUES ('Возвращение короля', '978-5-17-084323-8', 'Финальная часть трилогии Толкина о решающей битве за Средиземье.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-084323-8' AND a.name = 'Джон Р. Р. Толкин';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-084323-8' AND c.name = 'Фэнтези';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-084323-8' AND p.name = 'АСТ';

-- Книга 38
INSERT INTO books (name, isbn, description) VALUES ('Вино из одуванчиков', '978-5-699-18234-1', 'Автобиографическая, наполненная теплом повесть Рэя Брэдбери о лете двенадцатилетнего мальчика.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-18234-1' AND a.name = 'Рэй Брэдбери';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-18234-1' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-18234-1' AND p.name = 'Эксмо';

-- Книга 39
INSERT INTO books (name, isbn, description) VALUES ('Идиот', '978-5-389-04112-9', 'Роман Федора Достоевского о прекрасном и чистом человеке князе Мышкине в жестоком мире.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-04112-9' AND a.name = 'Федор Достоевский';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-04112-9' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-04112-9' AND p.name = 'Азбука';

-- Книга 40
INSERT INTO books (name, isbn, description) VALUES ('Высший замысел', '978-5-91671-333-6', 'Совместный научно-популярный труд Стивена Хокинга, предлагающий новый взгляд на законы Вселенной.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-91671-333-6' AND a.name = 'Стивен Хокинг';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-91671-333-6' AND c.name = 'Научпоп';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-91671-333-6' AND p.name = 'Альпина Паблишер';

--changeset victor:12-prod-authors-part5 context:prod
--comment: Добавление авторов для книг 41-50
INSERT INTO authors (name, description) VALUES
                                            ('Уолтер Айзексон', 'Американский журналист, писатель и биограф, автор известных биографий Стива Джобса и Илона Маска.'),
                                            ('Рэй Далио', 'Американский миллиардер, финансист, основатель инвестиционной фирмы Bridgewater Associates.'),
                                            ('Джордж Мартин', 'Современный американский писатель-фантаст, сценарист и продюсер, автор «Песни Льда и Огня».'),
                                            ('Агата Кристи', 'Знаменитая английская писательница, один из самых известных авторов детективной прозы в истории.')
    ON CONFLICT (name) DO NOTHING;


--changeset victor:13-prod-books-part5 context:prod
--comment: Книги 41-50
-- Книга 41
INSERT INTO books (name, isbn, description) VALUES ('Стив Джобс', '978-5-91671-256-8', 'Эксклюзивная биография основателя Apple, написанная Уолтером Айзексоном на основе сотен интервью.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-91671-256-8' AND a.name = 'Уолтер Айзексон';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-91671-256-8' AND c.name = 'Научпоп';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-91671-256-8' AND p.name = 'Альпина Паблишер';

-- Книга 42
INSERT INTO books (name, isbn, description) VALUES ('Принципы. Жизнь и работа', '978-5-699-99441-1', 'Правила жизни и ведения бизнеса от одного из самых влиятельных людей планеты Рэя Далио.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-99441-1' AND a.name = 'Рэй Далио';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-99441-1' AND c.name = 'Психология';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-99441-1' AND p.name = 'МИФ';

-- Книга 43
INSERT INTO books (name, isbn, description) VALUES ('Игра престолов', '978-5-17-078023-1', 'Первая книга культовой эпической саги «Песнь Льда и Огня», легшая в основу мирового сериала.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-078023-1' AND a.name = 'Джордж Мартин';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-078023-1' AND c.name = 'Фэнтези';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-078023-1' AND p.name = 'АСТ';

-- Книга 44
INSERT INTO books (name, isbn, description) VALUES ('Убийство в Восточном экспрессе', '978-5-699-88771-4', 'Один из самых знаменитых детективов Агаты Кристи со знаменитым сыщиком Эркюлем Пуаро.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-88771-4' AND a.name = 'Агата Кристи';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-88771-4' AND c.name = 'Детектив';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-88771-4' AND p.name = 'Эксмо';

-- Книга 45
INSERT INTO books (name, isbn, description) VALUES ('Десять негритят', '978-5-699-55112-9', 'Культовый детективный шедевр, признанный самой продаваемой книгой Агаты Кристи.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-55112-9' AND a.name = 'Агата Кристи';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-55112-9' AND c.name = 'Детектив';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-55112-9' AND p.name = 'Эксмо';

-- Книга 46
INSERT INTO books (name, isbn, description) VALUES ('Битва королей', '978-5-17-080556-2', 'Вторая книга саги Джорджа Мартина, где Семь Королевств разрывает гражданская война.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-080556-2' AND a.name = 'Джордж Мартин';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-080556-2' AND c.name = 'Фэнтези';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-080556-2' AND p.name = 'АСТ';

-- Книга 47
INSERT INTO books (name, isbn, description) VALUES ('Буря мечей', '978-5-17-082441-1', 'Продолжение борьбы за Железный трон, полное неожиданных предательств и эпических сражений.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-082441-1' AND a.name = 'Джордж Мартин';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-082441-1' AND c.name = 'Фэнтези';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-082441-1' AND p.name = 'АСТ';

-- Книга 48
INSERT INTO books (name, isbn, description) VALUES ('Леонардо да Винчи', '978-5-9614-6655-2', 'Глубокое исследование жизненного и творческого пути величайшего гения Возрождения от Айзексона.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-9614-6655-2' AND a.name = 'Уолтер Айзексон';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-9614-6655-2' AND c.name = 'Научпоп';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-9614-6655-2' AND p.name = 'Альпина Паблишер';

-- Книга 49
INSERT INTO books (name, isbn, description) VALUES ('Капитанская дочка', '978-5-389-00123-2', 'Историческая повесть Александра Пушкина о временах пугачевского бунта, чести и любви.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-00123-2' AND a.name = 'Александр Пушкин';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-00123-2' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-00123-2' AND p.name = 'Азбука';

-- Книга 50
INSERT INTO books (name, isbn, description) VALUES ('Записки охотника', '978-5-17-074411-2', 'Сборник потрясающих реалистичных рассказов Ивана Тургенева о жизни русской деревни.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-074411-2' AND a.name = 'Иван Тургенев';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-074411-2' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-074411-2' AND p.name = 'АСТ';

--changeset victor:14-prod-authors-part6 context:prod
--comment: Добавление авторов для книг 51-60
INSERT INTO authors (name, description) VALUES
                                            ('Альбер Камю', 'Французский прозаик, философ, эссеист, лауреат Нобелевской премии по литературе.'),
                                            ('Франц Кафка', 'Один из крупнейших немецкоязычных писателей XX века, мастер литературы абсурда.'),
                                            ('Джек Лондон', 'Знаменитый американский писатель, общественный деятель, автор приключенческих рассказов.'),
                                            ('Герберт Уэллс', 'Британский писатель и публицист, один из основоположников научно-популярной фантастики.')
    ON CONFLICT (name) DO NOTHING;


--changeset victor:15-prod-books-part6 context:prod
--comment: Книги 51-60
-- Книга 51
INSERT INTO books (name, isbn, description) VALUES ('Посторонний', '978-5-389-08112-6', 'Повесть Альбера Камю, ставшая своеобразным манифестом европейской философии экзистенциализма.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-08112-6' AND a.name = 'Альбер Камю';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-08112-6' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-08112-6' AND p.name = 'Азбука';

-- Книга 52
INSERT INTO books (name, isbn, description) VALUES ('Превращение', '978-5-389-05244-1', 'Знаменитая новелла Франца Кафки, описывающая трагическое одиночество человека перед лицом абсурда.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-05244-1' AND a.name = 'Франц Кафка';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-05244-1' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-05244-1' AND p.name = 'Азбука';

-- Книга 53
INSERT INTO books (name, isbn, description) VALUES ('Мартин Иден', '978-5-17-091444-2', 'Роман Джека Лондона о тяжелом пути простого моряка к вершинам литературного признания.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-091444-2' AND a.name = 'Джек Лондон';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-091444-2' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-091444-2' AND p.name = 'АСТ';

-- Книга 54
INSERT INTO books (name, isbn, description) VALUES ('Машина времени', '978-5-699-77112-0', 'Первый крупный научно-фантастический роман Герберта Уэллса о путешествии в далекое будущее.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-77112-0' AND a.name = 'Герберт Уэллс';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-77112-0' AND c.name = 'Фантастика';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-77112-0' AND p.name = 'Эксмо';

-- Книга 55
INSERT INTO books (name, isbn, description) VALUES ('Война миров', '978-5-699-88334-1', 'Классика фантастической литературы Уэллса о вторжении марсиан на Землю.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-88334-1' AND a.name = 'Герберт Уэллс';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-88334-1' AND c.name = 'Фантастика';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-88334-1' AND p.name = 'Эксмо';

-- Книга 56
INSERT INTO books (name, isbn, description) VALUES ('Белый Клык', '978-5-17-080221-3', 'Приключенческая повесть Джека Лондона, главным героем которой является одомашненный волк.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-080221-3' AND a.name = 'Джек Лондон';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-080221-3' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-080221-3' AND p.name = 'АСТ';

-- Книга 57
INSERT INTO books (name, isbn, description) VALUES ('Процесс', '978-5-389-01122-7', 'Один из самых загадочных и пугающих романов Франца Кафки, опубликованный посмертно.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-01122-7' AND a.name = 'Франц Кафка';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-01122-7' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-01122-7' AND p.name = 'Азбука';

-- Книга 58
INSERT INTO books (name, isbn, description) VALUES ('Чума', '978-5-389-09944-2', 'Роман-притча Альбера Камю о борьбе жителей алжирского города с внезапной смертоносной эпидемией.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-09944-2' AND a.name = 'Альбер Камю';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-09944-2' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-09944-2' AND p.name = 'Азбука';

-- Книга 59
INSERT INTO books (name, isbn, description) VALUES ('Пир во время чумы', '978-5-389-00256-4', 'Драматическое произведение Александра Пушкина, входящее в цикл Маленьких трагедий.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-00256-4' AND a.name = 'Александр Пушкин';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-00256-4' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-00256-4' AND p.name = 'Азбука';

-- Книга 60
INSERT INTO books (name, isbn, description) VALUES ('Человек-невидимка', '978-5-699-33441-9', 'Захватывающий роман Герберта Уэллса о гениальном ученом, совершившем роковое открытие.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-33441-9' AND a.name = 'Герберт Уэллс';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-33441-9' AND c.name = 'Фантастика';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-33441-9' AND p.name = 'Эксмо';

--changeset victor:16-prod-authors-part7 context:prod
--comment: Добавление авторов для книг 61-70
INSERT INTO authors (name, description) VALUES
                                            ('Эрнест Хемингуэй', 'Знаменитый американский писатель, журналист, лауреат Нобелевской премии по литературе.'),
                                            ('Фрэнсис Скотт Фицджеральд', 'Американский писатель, признанный классик литературы «века джаза».'),
                                            ('Зигмунд Фрейд', 'Австрийский психолог, психоаналитик, психиатр и невролог, основатель психоанализа.')
    ON CONFLICT (name) DO NOTHING;


--changeset victor:17-prod-books-part7 context:prod
--comment: Книги 61-70
-- Книга 61
INSERT INTO books (name, isbn, description) VALUES ('Старик и море', '978-5-17-088112-6', 'Повесть Эрнеста Хемингуэя о кубинском рыбаке Сантьяго и его борьбе с гигантским марлином.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-088112-6' AND a.name = 'Эрнест Хемингуэй';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-088112-6' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-088112-6' AND p.name = 'АСТ';

-- Книга 62
INSERT INTO books (name, isbn, description) VALUES ('Великий Гэтсби', '978-5-699-77552-1', 'Шедевр Фицджеральда об эпохе процветания, больших надежд и трагической любви в Америке 1920-х годов.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-77552-1' AND a.name = 'Фрэнсис Скотт Фицджеральд';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-77552-1' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-77552-1' AND p.name = 'Эксмо';

-- Книга 63
INSERT INTO books (name, isbn, description) VALUES ('Толкование сновидений', '978-5-91671-881-2', 'Фундаментальный труд Зигмунда Фрейда, посвященный анализу ночных грез человека.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-91671-881-2' AND a.name = 'Зигмунд Фрейд';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-91671-881-2' AND c.name = 'Психология';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-91671-881-2' AND p.name = 'Альпина Паблишер';

-- Книга 64
INSERT INTO books (name, isbn, description) VALUES ('По ком звонит колокол', '978-5-17-099123-5', 'Один из лучших романов Хемингуэя, рассказывающий об участии добровольца в Гражданской войне в Испании.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-099123-5' AND a.name = 'Эрнест Хемингуэй';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-099123-5' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-099123-5' AND p.name = 'АСТ';

-- Книга 65
INSERT INTO books (name, isbn, description) VALUES ('Ночь нежна', '978-5-699-12255-4', 'Глубоко психологический, во многом автобиографический роман Скотта Фицджеральда.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-12255-4' AND a.name = 'Фрэнсис Скотт Фицджеральд';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-12255-4' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-12255-4' AND p.name = 'Эксмо';

-- Книга 66
INSERT INTO books (name, isbn, description) VALUES ('Психопатология обыденной жизни', '978-5-17-084221-7', 'Знаменитое исследование Фрейда о природе оговорок, забывания слов и случайных действий.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-084221-7' AND a.name = 'Зигмунд Фрейд';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-084221-7' AND c.name = 'Психология';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-084221-7' AND p.name = 'АСТ';

-- Книга 67
INSERT INTO books (name, isbn, description) VALUES ('Праздник, который всегда с тобой', '978-5-17-077551-2', 'Книга воспоминаний Хемингуэя о его жизни в Париже в кругу писателей «потерянного поколения».') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-077551-2' AND a.name = 'Эрнест Хемингуэй';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-077551-2' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-077551-2' AND p.name = 'АСТ';

-- Книга 68
INSERT INTO books (name, isbn, description) VALUES ('По ту сторону принципа удовольствия', '978-5-91671-445-9', 'Философско-психологический очерк Зигмунда Фрейда, вводящий концепцию влечения к смерти.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-91671-445-9' AND a.name = 'Зигмунд Фрейд';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-91671-445-9' AND c.name = 'Психология';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-91671-445-9' AND p.name = 'Альпина Паблишер';

-- Книга 69
INSERT INTO books (name, isbn, description) VALUES ('Повести Белкина', '978-5-389-00441-2', 'Цикл повестей Александра Пушкина, ставших шедевром русской реалистической прозы.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-00441-2' AND a.name = 'Александр Пушкин';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-00441-2' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-00441-2' AND p.name = 'Азбука';

-- Книга 70
INSERT INTO books (name, isbn, description) VALUES ('Записки сумасшедшего', '978-5-17-094551-0', 'Знаменитая сатирическая повесть Николая Гоголя о мелком чиновнике Аксентии Поприщине.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-094551-0' AND a.name = 'Николай Гоголь';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-094551-0' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-094551-0' AND p.name = 'АСТ';

--changeset victor:18-prod-authors-part8 context:prod
--comment: Добавление авторов для книг 71-80
INSERT INTO authors (name, description) VALUES
                                            ('Ричард Фейнман', 'Выдающийся американский физик-теоретик, один из создателей квантовой электродинамики, лауреат Нобелевской премии.'),
                                            ('Оливер Сакс', 'Британский невролог, писатель и популяризатор медицины, автор удивительных клинических историй.'),
                                            ('Александр Куприн', 'Знаменитый русский писатель, реалист, мастер короткого рассказа и психологической повести.')
    ON CONFLICT (name) DO NOTHING;


--changeset victor:19-prod-books-part8 context:prod
--comment: Книги 71-80
-- Книга 71
INSERT INTO books (name, isbn, description) VALUES ('Вы, конечно, шутите, мистер Фейнман!', '978-5-17-091122-3', 'Потрясающие автобиографические рассказы гениального физика, полные юмора и житейской мудрости.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-091122-3' AND a.name = 'Ричард Фейнман';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-091122-3' AND c.name = 'Научпоп';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-091122-3' AND p.name = 'АСТ';

-- Книга 72
INSERT INTO books (name, isbn, description) VALUES ('Человек, который принял жену за шляпу', '978-5-17-088554-1', 'Глубокая и трогательная книга Оливера Сакса о необычных пациентах с нарушениями психики и их адаптации.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-088554-1' AND a.name = 'Оливер Сакс';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-088554-1' AND c.name = 'Психология';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-088554-1' AND p.name = 'АСТ';

-- Книга 73
INSERT INTO books (name, isbn, description) VALUES ('Гранатовый браслет', '978-5-389-02114-5', 'Пронзительная и трагическая повесть Александра Куприна о безответной, но чистой и святой любви.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-02114-5' AND a.name = 'Александр Куприн';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-02114-5' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-02114-5' AND p.name = 'Азбука';

-- Книга 74
INSERT INTO books (name, isbn, description) VALUES ('Какое тебе дело до того, что думают другие?', '978-5-17-100441-2', 'Вторая часть автобиографических заметок Фейнмана, включающая расследование катастрофы шаттла Челленджер.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-100441-2' AND a.name = 'Ричард Фейнман';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-100441-2' AND c.name = 'Научпоп';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-100441-2' AND p.name = 'АСТ';

-- Книга 75
INSERT INTO books (name, isbn, description) VALUES ('Антрополог на Марсе', '978-5-17-094112-4', 'Семь парадоксальных историй от Оливера Сакса о силе человеческого духа и скрытых резервах мозга.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-094112-4' AND a.name = 'Оливер Сакс';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-094112-4' AND c.name = 'Психология';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-094112-4' AND p.name = 'АСТ';

-- Книга 76
INSERT INTO books (name, isbn, description) VALUES ('Олеся', '978-5-389-05112-7', 'Повесть Куприна о любви городского интеллигента и лесной колдуньи Олеси, живущей в гармонии с природой.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-05112-7' AND a.name = 'Александр Куприн';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-05112-7' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-05112-7' AND p.name = 'Азбука';

-- Книга 77
INSERT INTO books (name, isbn, description) VALUES ('Поединок', '978-5-389-08256-4', 'Психологическая повесть Куприна, описывающая жесткие нравы армейской среды на рубеже веков.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-08256-4' AND a.name = 'Александр Куприн';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-08256-4' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-08256-4' AND p.name = 'Азбука';

-- Книга 78
INSERT INTO books (name, isbn, description) VALUES ('Радость познания', '978-5-9614-4455-2', 'Сборник лучших коротких работ и интервью Ричарда Фейнмана о красоте научных открытий.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-9614-4455-2' AND a.name = 'Ричард Фейнман';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-9614-4455-2' AND c.name = 'Научпоп';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-9614-4455-2' AND p.name = 'Альпина Паблишер';

-- Книга 79
INSERT INTO books (name, isbn, description) VALUES ('Крылья', '978-5-17-099234-5', 'Ранние повести и рассказы Александра Куприна о становлении человеческого характера.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-099234-5' AND a.name = 'Александр Куприн';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-099234-5' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-099234-5' AND p.name = 'АСТ';

-- Книга 80
INSERT INTO books (name, isbn, description) VALUES ('Галлюцинации', '978-5-91671-255-4', 'Профессиональный и захватывающий анализ Оливера Сакса феноменов ложного восприятия человеческого мозга.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-91671-255-4' AND a.name = 'Оливер Сакс';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-91671-255-4' AND c.name = 'Психология';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-91671-255-4' AND p.name = 'Альпина Паблишер';

--changeset victor:20-prod-authors-part9 context:prod
--comment: Добавление авторов для книг 81-90
INSERT INTO authors (name, description) VALUES
                                            ('Олдос Хаксли', 'Английский писатель, новеллист и философ, автор знаменитого романа-антиутопии.'),
                                            ('Станислав Лем', 'Всемирно известный польский писатель-фантаст, сатирик, философ и футуролог.'),
                                            ('Александр Блок', 'Выдающийся русский поэт Серебряного века, драматург, классик русской литературы.')
    ON CONFLICT (name) DO NOTHING;


--changeset victor:21-prod-books-part9 context:prod
--comment: Книги 81-90
-- Книга 81
INSERT INTO books (name, isbn, description) VALUES ('О дивный новый мир', '978-5-17-094321-4', 'Культовая антиутопия Олдоса Хаксли о генетически программируемом обществе потребления.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-094321-4' AND a.name = 'Олдос Хаксли';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-094321-4' AND c.name = 'Фантастика';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-094321-4' AND p.name = 'АСТ';

-- Книга 82
INSERT INTO books (name, isbn, description) VALUES ('Солярис', '978-5-699-88223-1', 'Шедевр Станислава Лема о поиске контакта с разумным океаном далекой планеты.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-88223-1' AND a.name = 'Станислав Лем';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-88223-1' AND c.name = 'Фантастика';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-88223-1' AND p.name = 'Эксмо';

-- Книга 83
INSERT INTO books (name, isbn, description) VALUES ('Стихотворения и поэмы', '978-5-389-01124-5', 'Полное собрание лирики Александра Блока, включая знаменитую поэму «Двенадцать».') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-01124-5' AND a.name = 'Александр Блок';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-01124-5' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-01124-5' AND p.name = 'Азбука';

-- Книга 84
INSERT INTO books (name, isbn, description) VALUES ('Обезьяна и сущность', '978-5-17-100552-1', 'Язвительный и пророческий роман-предостережение Хаксли о мире после ядерного апокалипсиса.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-100552-1' AND a.name = 'Олдос Хаксли';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-100552-1' AND c.name = 'Фантастика';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-100552-1' AND p.name = 'АСТ';

-- Книга 85
INSERT INTO books (name, isbn, description) VALUES ('Звездные дневники Ийона Тихого', '978-5-699-11224-7', 'Цикл иронических фантастических рассказов Станислава Лема о путешествиях космического исследователя.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-11224-7' AND a.name = 'Станислав Лем';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-11224-7' AND c.name = 'Фантастика';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-11224-7' AND p.name = 'Эксмо';

-- Книга 86
INSERT INTO books (name, isbn, description) VALUES ('Остров', '978-5-17-088114-1', 'Финальный роман Хаксли, представляющий собой утопический противовес его известной антиутопии.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-088114-1' AND a.name = 'Олдос Хаксли';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-088114-1' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-088114-1' AND p.name = 'АСТ';

-- Книга 87
INSERT INTO books (name, isbn, description) VALUES ('Кибериада', '978-5-699-99551-0', 'Сборник юмористических новелл Станислава Лема о вселенной, населенной роботами и разумными машинами.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-99551-0' AND a.name = 'Станислав Лем';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-99551-0' AND c.name = 'Фантастика';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-99551-0' AND p.name = 'Эксмо';

-- Книга 88
INSERT INTO books (name, isbn, description) VALUES ('Эдем', '978-5-699-44112-9', 'Классический научно-фантастический роман Лема о крушении земной экспедиции на неизведанной планете.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-44112-9' AND a.name = 'Станислав Лем';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-44112-9' AND c.name = 'Фантастика';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-44112-9' AND p.name = 'Эксмо';

-- Книга 89
INSERT INTO books (name, isbn, description) VALUES ('Незнакомка', '978-5-389-02554-1', 'Сборник лучших стихотворений и пьес Александра Блока периода расцвета символизма.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-02554-1' AND a.name = 'Александр Блок';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-02554-1' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-02554-1' AND p.name = 'Азбука';

-- Книга 90
INSERT INTO books (name, isbn, description) VALUES ('Двенадцать', '978-5-389-09551-2', 'Знаменитое иллюстрированное издание главной революционной поэмы Александра Блока.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-09551-2' AND a.name = 'Александр Блок';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-09551-2' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-09551-2' AND p.name = 'Азбука';

--changeset victor:22-prod-authors-final context:prod
--comment: Добавление авторов для финальных книг 91-100
INSERT INTO authors (name, description) VALUES
                                            ('Оскар Уайльд', 'Знаменитый ирландский писатель, поэт, драматург и эссеист, яркий представитель эстетизма.'),
                                            ('Редьярд Киплинг', 'Английский писатель, поэт и новеллист, первый английский лауреат Нобелевской премии по литературе.'),
                                            ('Александр Грин', 'Русский писатель-прозаик и поэт, представитель романтического реализма, создатель вымышленной страны.')
    ON CONFLICT (name) DO NOTHING;


--changeset victor:23-prod-books-final context:prod
--comment: Книги 91-100
-- Книга 91
INSERT INTO books (name, isbn, description) VALUES ('Портрет Дориана Грея', '978-5-389-02111-9', 'Единственный опубликованный роман Оскара Уайльда, ставший шедевром английской прозы.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-02111-9' AND a.name = 'Оскар Уайльд';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-02111-9' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-02111-9' AND p.name = 'Азбука';

-- Книга 92
INSERT INTO books (name, isbn, description) VALUES ('Книга джунглей', '978-5-17-094115-3', 'Знаменитый сборник рассказов Редьярда Киплинга об удивительных приключениях мальчика Маугли.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-094115-3' AND a.name = 'Редьярд Киплинг';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-094115-3' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-094115-3' AND p.name = 'АСТ';

-- Книга 93
INSERT INTO books (name, isbn, description) VALUES ('Алые паруса', '978-5-699-44221-3', 'Прекрасная и трогательная повесть-феерия Александра Грина о непоколебимой вере в чудо и мечту.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-44221-3' AND a.name = 'Александр Грин';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-44221-3' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-44221-3' AND p.name = 'Эксмо';

-- Книга 94
INSERT INTO books (name, isbn, description) VALUES ('Кентервильское привидение', '978-5-389-05512-4', 'Иронично-юмористическая повесть Оскара Уайльда о несчастном британском призраке и американской семье.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-05512-4' AND a.name = 'Оскар Уайльд';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-05512-4' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-05512-4' AND p.name = 'Азбука';

-- Книга 95
INSERT INTO books (name, isbn, description) VALUES ('Ким', '978-5-17-088223-4', 'Один из лучших приключенческих романов Киплинга, посвященный «Большой игре» разведок в Индии.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-088223-4' AND a.name = 'Редьярд Киплинг';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-088223-4' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-088223-4' AND p.name = 'АСТ';

-- Книга 96
INSERT INTO books (name, isbn, description) VALUES ('Бегущая по волнам', '978-5-699-11554-1', 'Романтический и мистический роман Александра Грина о поиске идеала и вере в неизведанное.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-699-11554-1' AND a.name = 'Александр Грин';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-699-11554-1' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-699-11554-1' AND p.name = 'Эксмо';

-- Книга 97
INSERT INTO books (name, isbn, description) VALUES ('Тюрьма народов', '978-5-17-094556-3', 'Поздние публицистические очерки и рассказы Редьярда Киплинга на исторические темы.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-094556-3' AND a.name = 'Редьярд Киплинг';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-094556-3' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-094556-3' AND p.name = 'АСТ';

-- Книга 98
INSERT INTO books (name, isbn, description) VALUES ('De Profundis', '978-5-389-08554-2', 'Глубокое и трагическое письмо-исповедь Оскара Уайльда, написанное им во время тюремного заключения.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-08554-2' AND a.name = 'Оскар Уайльд';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-08554-2' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-08554-2' AND p.name = 'Азбука';

-- Книга 99
INSERT INTO books (name, isbn, description) VALUES ('Баллада Редингской тюрьмы', '978-5-389-09911-3', 'Поэтический шедевр Оскара Уайльда, отражающий его тяжелый душевный опыт и переживания.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-389-09911-3' AND a.name = 'Оскар Уайльд';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-389-09911-3' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-389-09911-3' AND p.name = 'Азбука';

-- Книга 100
INSERT INTO books (name, isbn, description) VALUES ('Искатели приключений', '978-5-17-099115-4', 'Сборник малоизвестных романтических рассказов Александра Грина о морских странствиях.') ON CONFLICT (isbn) DO NOTHING;
INSERT INTO books_authors (book_id, author_id) SELECT b.id, a.id FROM books b, authors a WHERE b.isbn = '978-5-17-099115-4' AND a.name = 'Alexander Grin';
INSERT INTO books_categories (book_id, category_id) SELECT b.id, c.id FROM books b, categories c WHERE b.isbn = '978-5-17-099115-4' AND c.name = 'Классическая литература';
INSERT INTO books_publishers (book_id, publisher_id) SELECT b.id, p.id FROM books b, publishers p WHERE b.isbn = '978-5-17-099115-4' AND p.name = 'АСТ';