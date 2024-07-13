package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemShort;

import java.util.List;
import java.util.Set;


public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findByOwnerIdOrderByIdAsc(Long ownerId, Pageable pageable);
    List<ItemShort> findAllByRequestIdIn(Set<Long> requestIds);
    @Query("SELECT new ru.practicum.shareit.item.model.ItemShort(" +
            "it.id, " +
            "it.name, " +
            "it.description, " +
            "it.request.id, " +
            "it.available) " +
            "FROM Item as it " +
            "WHERE it.request.id = :requestId")
    List<ItemShort> findAllByRequestId(Long requestId);

    @Query("select it " +
            "from Item as it " +
            "where it.available is true and " +
            "(upper(it.name) like upper(concat('%', ?1, '%')) or " +
            "upper(it.description) like upper(concat('%', ?1, '%'))) ")
    Page<Item> searchTextInNameOrDescription(String text, Pageable pageable);
}