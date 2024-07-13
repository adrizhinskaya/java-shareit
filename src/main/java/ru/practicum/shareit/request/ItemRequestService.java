package ru.practicum.shareit.request;

import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestGetDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestGetDto> getAllByRequesterId(Long requesterId);

    List<ItemRequestGetDto> getAll(Long requesterId, int from, int size);

    ItemRequestGetDto getById(Long userId, Long requestId);
}
