package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository {
    public Item create(Long userId, ItemDto itemDto);

    public Item getById(Long id);

    public Collection<Item> getAll(Long userId);

    public Collection<Item> getFromSearch(String text);

    public Item update(Long userId, Long itemId, Item item);

    public boolean itemExists(Long id);
}