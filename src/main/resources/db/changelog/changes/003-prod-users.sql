--liquibase formatted sql

--changeset victor:3-prod-users context:prod
--comment: Создание начальных пользователей для промышленной базы PostgreSQL
INSERT INTO users (username, password, role)
VALUES ('user', '$2a$10$8.UnVuG9HHgN3vN6Y8.vXOCyBvB9qA78Yk7PBM9B.VreApxvM4B66', 'ROLE_USER')
    ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, role)
VALUES ('admin', '$2a$10$8.UnVuG9HHgN3vN6Y8.vXOCyBvB9qA78Yk7PBM9B.VreApxvM4B66', 'ROLE_ADMIN')
    ON CONFLICT (username) DO NOTHING;