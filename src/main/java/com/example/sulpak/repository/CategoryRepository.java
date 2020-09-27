package com.example.sulpak.repository;

import com.example.sulpak.model.Category;
import com.example.sulpak.model.MainGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findOneByUrl(String url);
    boolean existsByUrl(String url);
}
