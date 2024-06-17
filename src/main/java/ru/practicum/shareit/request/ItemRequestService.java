package ru.practicum.shareit.request;

import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestGetDto;

import java.util.Collection;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto);

    Collection<ItemRequestGetDto> getAllByRequesterId(Long requesterId);

    Collection<ItemRequestGetDto> getAll(Long userId, int from, int size);

    ItemRequestGetDto getById(Long userId, Long requestId);
}
