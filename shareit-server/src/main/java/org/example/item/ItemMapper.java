package org.example.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.example.booking.model.BookingShort;
import org.example.item.comment.CommentDto;
import org.example.item.model.Item;
import org.example.item.model.ItemDto;
import org.example.item.model.ItemGetDto;
import org.example.request.model.ItemRequest;
import org.example.user.model.User;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {
    public static ItemDto mapToItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() == null ? null : item.getRequest().getId())
                .build();
    }

    public static List<ItemDto> mapToItemDto(Iterable<Item> items) {
        List<ItemDto> dtos = new ArrayList<>();
        for (Item item : items) {
            dtos.add(mapToItemDto(item));
        }
        return dtos;
    }

    public static ItemGetDto mapToItemGetDto(Item item, List<BookingShort> lastBooking, List<BookingShort> nextBooking,
                                             List<CommentDto> comments) {
        return ItemGetDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking.isEmpty() ? null : lastBooking.get(0))
                .nextBooking(nextBooking.isEmpty() ? null : nextBooking.get(0))
                .comments(comments)
                .build();
    }


    public static Item mapToItem(ItemDto itemDto, User owner, ItemRequest request) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .request(request)
                .build();
    }
}