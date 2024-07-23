package org.example.request;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.example.item.model.ItemShort;
import org.example.request.model.ItemRequest;
import org.example.request.model.ItemRequestDto;
import org.example.request.model.ItemRequestGetDto;
import org.example.user.model.User;

import java.util.Collections;
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

    public static ItemRequestGetDto mapToItemRequestGetDto(ItemRequest request, List<ItemShort> itemShorts) {
        return ItemRequestGetDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(itemShorts == null ? Collections.emptyList() : itemShorts)
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
