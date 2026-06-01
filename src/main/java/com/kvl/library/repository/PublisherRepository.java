package com.kvl.library.repository;

import com.kvl.library.entity.Publisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {

    // Кастомный запрос: регистронезависимый поиск по части имени с пагинацией
    @Query("SELECT p FROM Publisher p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Publisher> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
}