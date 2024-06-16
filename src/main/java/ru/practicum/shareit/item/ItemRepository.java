package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemShort;

import java.util.Collection;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwnerIdOrderByIdAsc(Long ownerId);

    @Query("SELECT new ru.practicum.shareit.item.model.ItemShort(it.id, it.name, it.description, it.request.id, it.available) " +
            "FROM Item as it " +
            "WHERE it.request.id = :requestId")
    Collection<ItemShort> findAllByRequestId(Long requestId);

    @Query("select it " +
            "from Item as it " +
            "where it.available is true and " +
            "(upper(it.name) like upper(concat('%', ?1, '%')) or " +
            "upper(it.description) like upper(concat('%', ?1, '%'))) ")
    List<Item> search(String text);
}