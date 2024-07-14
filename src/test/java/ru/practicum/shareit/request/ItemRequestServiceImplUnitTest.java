package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.ItemShort;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestGetDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
public class ItemRequestServiceImplUnitTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemRequestRepository requestRepository;

    @InjectMocks
    private ItemRequestServiceImpl requestService;
    private User user;
    private ItemRequest request;
    private ItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = makeUser();
        request = makeItemRequest(user);
        requestDto = makeItemRequestDto(request);
    }

    @Test
    public void testCreateNotFound() {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> requestService.create(1L, requestDto));
        Mockito.verify(requestRepository, Mockito.never())
                .save(any(ItemRequest.class));
    }

    @Test
    public void testCreate() {
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(requestRepository.save(Mockito.any(ItemRequest.class))).thenReturn(request);

        ItemRequestDto resultRequest = requestService.create(user.getId(), requestDto);

        Mockito.verify(requestRepository, Mockito.times(1))
                .save(any(ItemRequest.class));
        assertEquals(request.getId(), resultRequest.getId());
        assertEquals(request.getDescription(), resultRequest.getDescription());
        assertEquals(request.getRequester().getId(), resultRequest.getRequesterId());
        assertEquals(request.getCreated(), resultRequest.getCreated());
    }

    @Test
    public void testGetAllByRequesterIdNotFound() {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> requestService.getAllByRequesterId(1L));
        Mockito.verify(requestRepository, Mockito.never())
                .findAllByRequester_IdOrderByCreatedDesc(any(Long.class));
        Mockito.verify(itemRepository, Mockito.never())
                .findAllByRequestIdIn(any(Set.class));
    }

    @Test
    public void testGetAllByRequesterId() {
        List<ItemRequest> itemRequests = List.of(request);
        List<ItemShort> itemShorts = List.of(
                new ItemShort(1L, "Name", "Description", 1L, true));
        List<ItemRequestGetDto> requestGetDtos = List.of(
                ItemRequestMapper.mapToItemRequestGetDto(itemRequests.get(0), itemShorts));

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(requestRepository.findAllByRequester_IdOrderByCreatedDesc(1L)).thenReturn(itemRequests);
        Mockito.when(itemRepository.findAllByRequestIdIn(Set.of(1L))).thenReturn(itemShorts);

        List<ItemRequestGetDto> resultRequests = requestService.getAllByRequesterId(user.getId());

        Mockito.verify(requestRepository, Mockito.times(1))
                .findAllByRequester_IdOrderByCreatedDesc(any(Long.class));
        Mockito.verify(itemRepository, Mockito.times(1))
                .findAllByRequestIdIn(any(Set.class));

        assertEquals(1, resultRequests.size());
        assertEquals(requestGetDtos.get(0), resultRequests.get(0));
    }

    @Test
    public void testGetByIdNotFound() {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> requestService.getById(1L, 1L));
        Mockito.verify(requestRepository, Mockito.never())
                .findAllByRequester_IdOrderByCreatedDesc(any(Long.class));
        Mockito.verify(itemRepository, Mockito.never())
                .findAllByRequestIdIn(any(Set.class));

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(requestRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(ItemRequestNotFoundException.class, () -> requestService.getById(1L, 1L));
        Mockito.verify(requestRepository, Mockito.never())
                .findAllByRequester_IdOrderByCreatedDesc(any(Long.class));
        Mockito.verify(itemRepository, Mockito.never())
                .findAllByRequestIdIn(any(Set.class));
    }

    @Test
    public void testGetById() {
        List<ItemShort> itemShorts = List.of(
                new ItemShort(1L, "Name", "Description", 1L, true));
        ItemRequestGetDto requestGetDto = ItemRequestMapper.mapToItemRequestGetDto(request, itemShorts);

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(requestRepository.findById(Mockito.any())).thenReturn(Optional.of(request));
        Mockito.when(itemRepository.findAllByRequestId(1L)).thenReturn(itemShorts);

        ItemRequestGetDto resultRequest = requestService.getById(user.getId(), request.getId());

        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(any(Long.class));
        Mockito.verify(itemRepository, Mockito.times(1))
                .findAllByRequestId(any(Long.class));
        assertEquals(requestGetDto, resultRequest);
    }

    private User makeUser() {
        return User.builder()
                .id(1L)
                .name("John")
                .email("john@example.com")
                .build();
    }

    public static ItemRequest makeItemRequest(User user) {
        return ItemRequest.builder()
                .id(1L)
                .description("description")
                .requester(user)
                .created(LocalDateTime.now())
                .build();
    }

    private ItemRequestDto makeItemRequestDto(ItemRequest request) {
        return ItemRequestDto.builder()
                .description(request.getDescription())
                .requesterId(request.getRequester().getId())
                .created(request.getCreated())
                .build();
    }
}
