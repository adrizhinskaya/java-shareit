package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.BookingShort;
import ru.practicum.shareit.exception.ItemBadRequestException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemGetDto;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
public class ItemServiceImplUnitTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User itemOwner;
    private User requester;
    private ItemRequest request;
    private Item item;
    private ItemDto itemDto;
    private CommentDto commentDto;
    private Comment comment;

    @BeforeEach
    void setUp() {
        itemOwner = makeUser(1L, "Пётр", "some@email.com");
        requester = makeUser(2L, "НеПётр", "any@email.com");
        request = makeItemRequest();
        item = makeItem();
        itemDto = makeItemDto();
        comment = makeComment();
        commentDto = makeCommentDto();
    }

    @Test
    public void testCreateNotFound() {
        assertThrows(UserNotFoundException.class, () -> itemService.create(1L, itemDto));

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(itemOwner));
        Mockito.when(requestRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(ItemRequestNotFoundException.class, () -> itemService.create(1L, itemDto));
        Mockito.verify(itemRepository, Mockito.never())
                .save(any(Item.class));
    }

    @Test
    public void testCreate() {
        Mockito.when(userRepository.findById(itemOwner.getId())).thenReturn(Optional.of(itemOwner));
        Mockito.when(requestRepository.findById(itemDto.getRequestId())).thenReturn(Optional.of(request));
        Mockito.when(itemRepository.save(Mockito.any(Item.class))).thenReturn(item);

        ItemDto resultItem = itemService.create(itemOwner.getId(), itemDto);

        Mockito.verify(itemRepository, Mockito.times(1))
                .save(any(Item.class));
        assertEquals(item.getId(), resultItem.getId());
        assertEquals(item.getName(), resultItem.getName());
        assertEquals(item.getDescription(), resultItem.getDescription());
        assertEquals(item.getAvailable(), resultItem.getAvailable());
        assertEquals(item.getRequest().getId(), resultItem.getRequestId());
    }

    @Test
    public void testAddCommentNotFound() {
        assertThrows(UserNotFoundException.class, () -> itemService.addComment(1L, 1L, commentDto));

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(itemOwner));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(ItemNotFoundException.class, () -> itemService.addComment(1L, 1L, commentDto));
        Mockito.verify(commentRepository, Mockito.never())
                .save(any(Comment.class));

        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.existsBookingByBooker_IdAndItem_IdAndEndBeforeAndStatusNotIn(
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        assertThrows(ItemBadRequestException.class, () -> itemService.addComment(1L, 1L, commentDto));
        Mockito.verify(commentRepository, Mockito.never())
                .save(any(Comment.class));
    }

    @Test
    public void testAddComment() {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(itemOwner));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.existsBookingByBooker_IdAndItem_IdAndEndBeforeAndStatusNotIn(
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(commentRepository.save(Mockito.any(Comment.class))).thenReturn(comment);

        CommentDto resultComment = itemService.addComment(requester.getId(), item.getId(), commentDto);

        Mockito.verify(commentRepository, Mockito.times(1))
                .save(any(Comment.class));
        assertEquals(comment.getId(), resultComment.getId());
        assertEquals(commentDto.getText(), resultComment.getText());
        assertEquals(commentDto.getAuthorName(), resultComment.getAuthorName());
        assertEquals(commentDto.getCreated(), resultComment.getCreated());
    }

    @Test
    public void testGetAllByOwnerIdNotFound() {
        assertThrows(UserNotFoundException.class, () -> itemService.getAllByOwnerId(1L, 0, 1));
        Mockito.verify(bookingRepository, Mockito.never())
                .findLastBookingsByItemIdsInAndStatusNotIn(any(Set.class), any(Set.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findNextBookingsByItemIdsInAndStatusNotIn(any(Set.class), any(Set.class));
        Mockito.verify(commentRepository, Mockito.never())
                .findAllByItemIdIn(any(Set.class));
    }

    @Test
    public void testGetFromSearchNotFound() {
        assertThrows(UserNotFoundException.class, () -> itemService.getFromSearch(
                1L, "Item", 0, 1));
        Mockito.verify(itemRepository, Mockito.never())
                .searchTextInNameOrDescription(any(String.class), any(Pageable.class));
    }

    @Test
    public void testGetFromSearch() {
        List<Item> itemList = List.of(item);
        Page<Item> page = new PageImpl<>(itemList, PageRequest.of(0, 1), itemList.size());

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(itemOwner));
        List<ItemDto> resultItems = itemService.getFromSearch(1L, " ", 0, 1);

        assertEquals(0, resultItems.size());

        Mockito.when(itemRepository.searchTextInNameOrDescription(Mockito.any(), Mockito.any())).thenReturn(page);
        resultItems = itemService.getFromSearch(1L, "te", 0, 1);

        Mockito.verify(itemRepository, Mockito.times(1))
                .searchTextInNameOrDescription(any(String.class), any(Pageable.class));
        assertEquals(1, resultItems.size());
        assertEquals(item.getId(), resultItems.get(0).getId());
        assertEquals(item.getName(), resultItems.get(0).getName());
        assertEquals(item.getDescription(), resultItems.get(0).getDescription());
        assertEquals(item.getAvailable(), resultItems.get(0).getAvailable());
        assertEquals(item.getRequest().getId(), resultItems.get(0).getRequestId());
    }

    @Test
    public void testGetByIdNotFound() {
        assertThrows(UserNotFoundException.class, () -> itemService.getById(1L, 1L));

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(itemOwner));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.getById(1L, 1L));
        Mockito.verify(bookingRepository, Mockito.never())
                .findLastBookingsByItemIdAndStatusNotIn(any(Long.class), any(Set.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findNextBookingsByItemIdAndStatusNotIn(any(Long.class), any(Set.class));
        Mockito.verify(commentRepository, Mockito.never())
                .findAllByItemId(any(Long.class));
    }

    @Test
    public void testGetById() {
        List<BookingShort> bookingShortList = List.of(new BookingShort(1L, 1L, 1L),
                new BookingShort(2L, 2L, 2L));
        List<Comment> commentList = List.of(comment);

        // userId is Owner
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(itemOwner));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findLastBookingsByItemIdAndStatusNotIn(
                Mockito.any(), Mockito.any())).thenReturn(bookingShortList);
        Mockito.when(bookingRepository.findNextBookingsByItemIdAndStatusNotIn(
                Mockito.any(), Mockito.any())).thenReturn(bookingShortList);
        Mockito.when(commentRepository.findAllByItemId(Mockito.any())).thenReturn(commentList);

        ItemGetDto resultItem = itemService.getById(itemOwner.getId(), 1L);

        assertEquals(item.getId(), resultItem.getId());
        assertEquals(item.getName(), resultItem.getName());
        assertEquals(item.getDescription(), resultItem.getDescription());
        assertEquals(item.getAvailable(), resultItem.getAvailable());
        assertEquals(bookingShortList.get(0), resultItem.getLastBooking());
        assertEquals(bookingShortList.get(0), resultItem.getNextBooking());
        assertEquals(commentList.size(), resultItem.getComments().size());
        assertEquals(comment.getId(), resultItem.getComments().get(0).getId());

        // userId is not Owner
        resultItem = itemService.getById(requester.getId(), 1L);

        assertEquals(item.getId(), resultItem.getId());
        assertEquals(item.getName(), resultItem.getName());
        assertEquals(item.getDescription(), resultItem.getDescription());
        assertEquals(item.getAvailable(), resultItem.getAvailable());
        assertNull(resultItem.getLastBooking());
        assertNull(resultItem.getNextBooking());
        assertEquals(commentList.size(), resultItem.getComments().size());
        assertEquals(comment.getId(), resultItem.getComments().get(0).getId());
    }

    @Test
    public void testUpdateNotFound() {
        assertThrows(UserNotFoundException.class, () -> itemService.update(1L, 1L, itemDto));

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(itemOwner));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(ItemNotFoundException.class, () -> itemService.update(1L, 1L, itemDto));

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(itemOwner));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(item));
        assertThrows(UserNotFoundException.class, () -> itemService.update(5L, 1L, itemDto));

        Mockito.verify(itemRepository, Mockito.never())
                .save(any(Item.class));
    }

    @Test
    public void testUpdate() {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(itemOwner));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(item));
        item.setName("NEW");
        Mockito.when(itemRepository.save(Mockito.any(Item.class))).thenReturn(item);

        itemDto.setName("NEW");
        ItemDto resultItem = itemService.update(1L, 1L, itemDto);
        Mockito.verify(itemRepository, Mockito.times(1))
                .save(any(Item.class));

        assertEquals(item.getId(), resultItem.getId());
        assertEquals(itemDto.getName(), resultItem.getName());
        assertEquals(itemDto.getDescription(), resultItem.getDescription());
        assertEquals(itemDto.getAvailable(), resultItem.getAvailable());
        assertEquals(itemDto.getRequestId(), resultItem.getRequestId());
    }

    private User makeUser(Long id, String name, String email) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();
    }

    private Item makeItem() {
        return Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .owner(itemOwner)
                .request(request)
                .build();
    }

    private ItemDto makeItemDto() {
        return ItemDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest().getId())
                .build();
    }

    private ItemRequest makeItemRequest() {
        return ItemRequest.builder()
                .id(1L)
                .description("description")
                .requester(requester)
                .created(LocalDateTime.now())
                .build();
    }

    private CommentDto makeCommentDto() {
        return CommentDto.builder()
                .text("text")
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    private Comment makeComment() {
        return Comment.builder()
                .id(1L)
                .text("text")
                .item(item)
                .author(requester)
                .created(LocalDateTime.now())
                .build();
    }
}
