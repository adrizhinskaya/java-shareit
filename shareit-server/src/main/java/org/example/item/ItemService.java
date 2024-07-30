package org.example.item;

import org.example.item.comment.CommentDto;
import org.example.item.model.ItemDto;
import org.example.item.model.ItemGetDto;

import java.util.List;

public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto);

    CommentDto addComment(Long userId, Long itemId, CommentDto comDto);

    List<ItemGetDto> getAllByOwnerId(Long userId, int from, int size);

    List<ItemDto> getFromSearch(Long userId, String text, int from, int size);

    ItemGetDto getById(Long userId, Long itemId);

    ItemDto update(Long userId, Long itemId, ItemDto newItem);
}
