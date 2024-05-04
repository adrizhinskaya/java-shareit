package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemService {
    public Item create(Long userId, ItemDto itemDto);

    public Collection<Item> getAll(Long userId);

    Collection<Item> getFromSearch(Long userId, String text);

    public Item getById(Long id);

    public Item update(Long userId, Long itemId, Item item);
}
