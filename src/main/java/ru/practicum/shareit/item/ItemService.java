package ru.practicum.shareit.item;

import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemGetDto;

import java.util.List;

public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto);

    CommentDto addComment(Long userId, Long itemId, CommentDto comDto);

    List<ItemGetDto> getAllByOwnerId(Long userId, int from, int size);

    List<ItemDto> getFromSearch(Long userId, String text, int from, int size);

    ItemGetDto getById(Long userId, Long itemId);

    ItemDto update(Long userId, Long itemId, ItemDto newItem);
}
