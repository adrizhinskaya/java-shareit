package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository {
    Item create(Long userId, ItemDto itemDto);

    Item getById(Long id);

    Collection<Item> getAll(Long userId);

    Collection<Item> getFromSearch(String text);

    Item update(Long userId, Item item, Item newItem);

    boolean itemExists(Long id);
}