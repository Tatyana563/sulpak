package com.example.sulpak.repository;

import com.example.sulpak.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item,Integer> {
    boolean existsByCode(Integer code);
    Optional<Item> findOneByCode(Integer code);
}
