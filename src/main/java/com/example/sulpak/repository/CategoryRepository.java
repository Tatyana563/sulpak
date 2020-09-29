package com.example.sulpak.repository;

import com.example.sulpak.model.Category;
import com.example.sulpak.model.MainGroup;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    boolean existsByUrl(String url);
    @Query("from Category as c ORDER BY c.id ASC")
    List<Category> getChunk(PageRequest pageable);
}
