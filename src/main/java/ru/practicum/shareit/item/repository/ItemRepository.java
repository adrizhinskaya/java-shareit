package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("select it " +
            "from Item as it " +
            "where it.available is true and " +
            "upper(it.name) like upper(concat('%', ?1, '%')) or " +
            "upper(it.description) like upper(concat('%', ?1, '%')) ")
    List<Item> search(String text);
}