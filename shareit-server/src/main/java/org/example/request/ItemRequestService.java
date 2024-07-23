package org.example.request;

import org.example.request.model.ItemRequestDto;
import org.example.request.model.ItemRequestGetDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestGetDto> getAllByRequesterId(Long requesterId);

    List<ItemRequestGetDto> getAll(Long requesterId, int from, int size);

    ItemRequestGetDto getById(Long userId, Long requestId);
}
