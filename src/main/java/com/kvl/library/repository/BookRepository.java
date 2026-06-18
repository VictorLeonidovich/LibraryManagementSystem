package com.kvl.library.repository;

import com.kvl.library.entity.Book;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    // Кастомный запрос: поиск по названию или по ISBN (регистронезависимый) с пагинацией
    @Query("SELECT b FROM Book b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchByNameOrIsbn(@Param("keyword") String keyword, Pageable pageable);

    // При вызове этого метода Hibernate сразу сделает JOIN таблиц категорий, авторов и издателей
    // FIX - Cannot lazily initialize collection of role...
    @EntityGraph(attributePaths = {"authors", "categories", "publishers"})
    @Override
    Optional<Book> findById(Long id);

    // При вызове этого метода Hibernate сразу сделает JOIN таблиц категорий, авторов и издателей
    // FIX - Cannot lazily initialize collection of role...
    @EntityGraph(attributePaths = {"authors", "categories", "publishers"})
    @Override
    List<Book> findAll();

    @Query("SELECT b.isbn FROM Book b")
    List<String> findTop10Isbns(Pageable pageable);
}