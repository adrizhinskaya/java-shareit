package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.ItemShort;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestGetDto;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRequestMapper {
    public static ItemRequestDto mapToItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requesterId(itemRequest.getRequester().getId())
                .created(itemRequest.getCreated())
                .build();
    }

    public static List<ItemRequestDto> mapToItemRequestDto(Iterable<ItemRequest> itemRequests) {
        List<ItemRequestDto> dtos = new ArrayList<>();
        for (ItemRequest itemRequest : itemRequests) {
            dtos.add(mapToItemRequestDto(itemRequest));
        }
        return dtos;
    }

    public static ItemRequestGetDto mapToItemRequestGetDto(ItemRequest request, List<ItemShort> itemShorts) {
        return ItemRequestGetDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(itemShorts)
                .build();
    }

    public static ItemRequest mapToItemRequest(ItemRequestDto itemRequestDto, User requester) {
        return ItemRequest.builder()
                .id(itemRequestDto.getId())
                .description(itemRequestDto.getDescription())
                .requester(requester)
                .created(itemRequestDto.getCreated())
                .build();
    }
}
