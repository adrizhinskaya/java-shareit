package ru.practicum.shareit.item;

import ru.practicum.shareit.item.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto);

    Collection<ItemDto> getAll(Long userId);

    Collection<ItemDto> getFromSearch(Long userId, String text);

    ItemDto getById(Long id);

    ItemDto update(Long userId, Long itemId, ItemDto newItem);
}
