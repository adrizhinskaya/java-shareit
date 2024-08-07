package org.example.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.example.booking.BookingRepository;
import org.example.booking.BookingStatus;
import org.example.booking.model.BookingShort;
import org.example.exception.ItemBadRequestException;
import org.example.exception.ItemNotFoundException;
import org.example.exception.ItemRequestNotFoundException;
import org.example.exception.UserNotFoundException;
import org.example.item.comment.Comment;
import org.example.item.comment.CommentDto;
import org.example.item.comment.CommentMapper;
import org.example.item.comment.CommentRepository;
import org.example.item.model.Item;
import org.example.item.model.ItemDto;
import org.example.item.model.ItemGetDto;
import org.example.request.ItemRequestRepository;
import org.example.request.model.ItemRequest;
import org.example.user.UserRepository;
import org.example.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        userExistCheck(userId);
        Set<BookingStatus> statusSet = Set.of(BookingStatus.REJECTED, BookingStatus.CANCELED);

        List<Item> items = findByOwnerIdOrderByIdAscWithPagination(userId, from, size);

        Set<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toSet());
        Map<Long, List<BookingShort>> lastBookings = bookingRepository
                .findLastBookingsByItemIdsInAndStatusNotIn(itemIds, statusSet).stream()
                .collect(Collectors.groupingBy(BookingShort::getItemId));
        Map<Long, List<BookingShort>> nextBookings = bookingRepository
                .findNextBookingsByItemIdsInAndStatusNotIn(itemIds, statusSet).stream()
                .collect(Collectors.groupingBy(BookingShort::getItemId));
        Map<Long, List<Comment>> comments = commentRepository
                .findAllByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        return items.stream()
                .map(item -> ItemMapper.mapToItemGetDto(item,
                        lastBookings.getOrDefault(item.getId(), Collections.emptyList()),
                        nextBookings.getOrDefault(item.getId(), Collections.emptyList()),
                        CommentMapper.mapToCommentDto(comments.getOrDefault(item.getId(), Collections.emptyList()))))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getFromSearch(Long userId, String text, int from, int size) {
        if (text.isBlank()) return Collections.emptyList();
        userExistCheck(userId);
        List<Item> items = searchTextInNameOrDescriptionWithPagination(text, from, size);
        return ItemMapper.mapToItemDto(items);
    }

    @Override
    public ItemGetDto getById(Long userId, Long itemId) {
        Set<BookingStatus> statusSet = Set.of(BookingStatus.REJECTED, BookingStatus.CANCELED);
        userExistCheck(userId);
        Item item = itemExistCheck(itemId);
        Pageable page = PageRequest.of(0, 1);

        boolean isOwner = userId.equals(item.getOwner().getId());
        List<BookingShort> last = isOwner ? bookingRepository.findLastBookingsByItemIdAndStatusNotIn(itemId, statusSet)
                : Collections.emptyList();
        List<BookingShort> next = isOwner ? bookingRepository.findNextBookingsByItemIdAndStatusNotIn(itemId, statusSet)
                : Collections.emptyList();
        List<CommentDto> comms = CommentMapper.mapToCommentDto(commentRepository.findAllByItemId(itemId));
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

    private List<Item> findByOwnerIdOrderByIdAscWithPagination(Long userId, int from, int size) {
        if (from % size == 0) {
            return itemRepository.findByOwnerIdOrderByIdAsc(userId, PageRequest.of(from / size, size))
                    .getContent();
        }

        int startPage = from / size;
        double nextPageElPercent = (double) from / size - startPage;
        int countOfNextPageEl = (int) Math.ceil(nextPageElPercent * size);
        int countOfStartPageEl = size - countOfNextPageEl;

        Pageable page = PageRequest.of(startPage, size);
        Page<Item> itemsPage = itemRepository.findByOwnerIdOrderByIdAsc(userId, page);
        List<Item> itemsList = itemsPage.getContent();

        if (itemsList.isEmpty()) {
            return itemsList;
        }

        List<Item> startPageItems = itemsList.subList(itemsList.size() - countOfStartPageEl, itemsList.size());

        if (itemsPage.hasNext()) {
            page = PageRequest.of(startPage + 1, size);
            itemsPage = itemRepository.findByOwnerIdOrderByIdAsc(userId, page);
            List<Item> nextPageItems = itemsPage.getContent().subList(0, countOfNextPageEl);
            return Stream.concat(
                    startPageItems.stream(),
                    nextPageItems.stream()).collect(Collectors.toList());
        }

        return startPageItems;
    }

    private List<Item> searchTextInNameOrDescriptionWithPagination(String text, int from, int size) {
        if (from % size == 0) {
            return itemRepository.searchTextInNameOrDescription(text, PageRequest.of(from / size, size))
                    .getContent();
        }

        int startPage = from / size;
        double nextPageElPercent = (double) from / size - startPage;
        int countOfNextPageEl = (int) Math.ceil(nextPageElPercent * size);
        int countOfStartPageEl = size - countOfNextPageEl;

        Pageable page = PageRequest.of(startPage, size);
        Page<Item> itemsPage = itemRepository.searchTextInNameOrDescription(text, page);
        List<Item> itemsList = itemsPage.getContent();

        if (itemsList.isEmpty()) {
            return itemsList;
        }

        List<Item> startPageItems = itemsList.subList(itemsList.size() - countOfStartPageEl, itemsList.size());

        if (itemsPage.hasNext()) {
            page = PageRequest.of(startPage + 1, size);
            itemsPage = itemRepository.searchTextInNameOrDescription(text, page);
            List<Item> nextPageItems = itemsPage.getContent().subList(0, countOfNextPageEl);
            return Stream.concat(
                    startPageItems.stream(),
                    nextPageItems.stream()).collect(Collectors.toList());
        }

        return startPageItems;
    }
}
