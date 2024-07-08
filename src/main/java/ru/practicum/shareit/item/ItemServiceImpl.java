package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.BookingShort;
import ru.practicum.shareit.exception.ItemBadRequestException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemGetDto;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    private User userExistCheck(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException("Для операций c вещами/комментариями нужно создать пользователя"));
    }

    private Item itemExistCheck(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new ItemNotFoundException("Вещь не найдена"));
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User user = userExistCheck(userId);
        ItemRequest request = itemDto.getRequestId() == null ? null :
                requestRepository.findById(itemDto.getRequestId()).orElseThrow(() ->
                        new ItemRequestNotFoundException("Запрос не найден"));

        Item item = itemRepository.save(ItemMapper.mapToItem(itemDto, user, request));
        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto comDto) {
        Set<BookingStatus> stateSet = Set.of(BookingStatus.REJECTED, BookingStatus.CANCELED);
        User user = userExistCheck(userId);
        Item item = itemExistCheck(itemId);

        if (!bookingRepository.existsBookingByBooker_IdAndItem_IdAndEndBeforeAndStatusNotIn(userId, itemId,
                LocalDateTime.now(), stateSet)) {
            throw new ItemBadRequestException("Отзыв невозможен, если не было букинга или букинг не закончен");
        }
        Comment comment = commentRepository.save(CommentMapper.mapToComment(comDto, user, item));
        return CommentMapper.mapToCommentDto(comment);
    }

    @Override
    public List<ItemGetDto> getAllByOwnerId(Long userId, int from, int size) {
        Set<BookingStatus> statusSet = Set.of(BookingStatus.REJECTED, BookingStatus.CANCELED);
        userExistCheck(userId);
        Pageable page = PageRequest.of(0, 1);

        Page<Item> items = itemRepository.findByOwnerIdOrderByIdAsc(userId, PageRequest.of(from / size, size));
        List<ItemGetDto> itemsDtos = new ArrayList<>(items.getSize());
        items.forEach(item -> itemsDtos.add(ItemMapper.mapToItemGetDto(item,
                bookingRepository.findLastBookingsByItem_IdAndStatusNotIn(item.getId(), statusSet, page).stream()
                        .findFirst().orElse(null),
                bookingRepository.findNextBookingsByItem_IdAndStatusNotIn(item.getId(), statusSet, page).stream()
                        .findFirst().orElse(null),
                CommentMapper.mapToCommentDto(commentRepository.findAllByItem_Id(item.getId())))));
        return itemsDtos;
    }

    @Override
    public List<ItemDto> getFromSearch(Long userId, String text, int from, int size) {
        if (text.isBlank()) return Collections.emptyList();
        userExistCheck(userId);
        Page<Item> items = itemRepository.search(text, PageRequest.of(from / size, size));
        return ItemMapper.mapToItemDto(items);
    }

    @Override
    public ItemGetDto getById(Long userId, Long itemId) {
        Set<BookingStatus> statusSet = Set.of(BookingStatus.REJECTED, BookingStatus.CANCELED);
        userExistCheck(userId);
        Item item = itemExistCheck(itemId);
        Pageable page = PageRequest.of(0, 1);

        boolean isOwner = userId.equals(item.getOwner().getId());
        BookingShort last = isOwner ? bookingRepository.findLastBookingsByItem_IdAndStatusNotIn(itemId, statusSet, page)
                .stream()
                .findFirst()
                .orElse(null) : null;
        BookingShort next = isOwner ? bookingRepository.findNextBookingsByItem_IdAndStatusNotIn(itemId, statusSet, page)
                .stream()
                .findFirst()
                .orElse(null) : null;
        List<CommentDto> comms = CommentMapper.mapToCommentDto(commentRepository.findAllByItem_Id(itemId));
        return ItemMapper.mapToItemGetDto(item, last, next, comms);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto newItem) {
        userExistCheck(userId);
        Item item = itemExistCheck(itemId);
        if (!userId.equals(item.getOwner().getId())) {
            throw new UserNotFoundException("Вещь может обновить только владелец");
        }
        if (newItem.getName() != null) {
            item.setName(newItem.getName());
        }
        if (newItem.getDescription() != null) {
            item.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null) {
            item.setAvailable(newItem.getAvailable());
        }
        Item updatedItem = itemRepository.save(item);
        return ItemMapper.mapToItemDto(updatedItem);
    }
}
