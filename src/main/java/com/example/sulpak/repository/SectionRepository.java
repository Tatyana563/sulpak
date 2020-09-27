package com.example.sulpak.repository;

import com.example.sulpak.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Integer> {
    Optional<Section> findOneByUrl(String url);
}
