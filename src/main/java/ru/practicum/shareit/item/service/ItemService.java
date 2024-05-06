package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemService {
    Item create(Long userId, ItemDto itemDto);

    Collection<Item> getAll(Long userId);

    Collection<Item> getFromSearch(Long userId, String text);

    Item getById(Long id);

    Item update(Long userId, Long itemId, Item item);
}
