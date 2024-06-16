package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
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
import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Попытка создания айтема от " +
                        "несуществующего пользователя"));

        ItemRequest request = itemDto.getRequestId() == null ? null :
                requestRepository.findById(itemDto.getRequestId()).orElseThrow(() ->
                new ItemRequestNotFoundException("Запрос не найден"));
        Item item = itemRepository.save(ItemMapper.mapToItem(itemDto, user, request));
        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto comDto) {
        Set<BookingStatus> stateSet = new HashSet<>();
        stateSet.add(BookingStatus.REJECTED);
        stateSet.add(BookingStatus.CANCELED);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Попытка создания отзыва от " +
                        "несуществующего пользователя"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь не найдена"));

        if (!bookingRepository.existsBookingByBooker_IdAndItem_IdAndEndBeforeAndStatusNotIn(userId, itemId,
                LocalDateTime.now(), stateSet)) {
            throw new ItemBadRequestException("ОТзыв невозможен если не было букинга или букинг не закончен");
        }
        Comment comment = commentRepository.save(CommentMapper.mapToComment(comDto, user, item));
        return CommentMapper.mapToCommentDto(comment);
    }

    @Override
    public Collection<ItemGetDto> getAllByOwnerId(Long userId) {
        Set<BookingStatus> stateSet = new HashSet<>();
        stateSet.add(BookingStatus.REJECTED);
        stateSet.add(BookingStatus.CANCELED);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Попытка просмотра айтемов от " +
                        "несуществующего пользователя"));
        List<Item> items = itemRepository.findByOwnerIdOrderByIdAsc(userId);
        List<ItemGetDto> itemsDtos = new ArrayList<>(items.size());
        items.forEach(item -> itemsDtos.add(ItemMapper.mapToItemDtoForGet(item,
                bookingRepository.findAllByItem_IdFromStartAsc(item.getId(), stateSet).stream()
                        .findFirst().orElse(null),
                bookingRepository.findAllByItem_IdAfterNowAsc(item.getId(), stateSet).stream()
                        .findFirst().orElse(null),
                CommentMapper.mapToCommentDto(commentRepository.findAllByItem_Id(item.getId())))));
        return itemsDtos;
    }

    @Override
    public Collection<ItemDto> getFromSearch(Long userId, String text) {
        if (text.isBlank()) return Collections.emptyList();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Попытка просмотра айтемов от " +
                        "несуществующего пользователя"));
        List<Item> items = itemRepository.search(text);
        return ItemMapper.mapToItemDto(items);
    }

    @Override
    public ItemGetDto getById(Long userId, Long itemId) {
        Set<BookingStatus> stateSet = new HashSet<>();
        stateSet.add(BookingStatus.REJECTED);
        stateSet.add(BookingStatus.CANCELED);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Попытка просмотра айтемов от " +
                        "несуществующего пользователя"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь не найдена"));
        boolean isOwner = userId.equals(item.getOwner().getId());
        BookingShort last = isOwner ? bookingRepository.findAllByItem_IdFromStartAsc(itemId, stateSet).stream()
                .findFirst().orElse(null) : null;
        BookingShort next = isOwner ? bookingRepository.findAllByItem_IdAfterNowAsc(itemId, stateSet).stream()
                .findFirst().orElse(null) : null;
        Collection<CommentDto> comms = CommentMapper.mapToCommentDto(commentRepository.findAllByItem_Id(itemId));
        return ItemMapper.mapToItemDtoForGet(item, last, next, comms);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto newItem) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Попытка обновления айтема от " +
                        "несуществующего пользователя"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Попытка обновления несуществующей вещи"));
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
