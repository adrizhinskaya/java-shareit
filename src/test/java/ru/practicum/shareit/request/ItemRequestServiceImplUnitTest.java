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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    }

    @Test
    public void testCreate() {
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(requestRepository.save(Mockito.any(ItemRequest.class))).thenReturn(request);

        ItemRequestDto resultRequest = requestService.create(user.getId(), requestDto);

        assertEquals(request.getId(), resultRequest.getId());
        assertEquals(request.getDescription(), resultRequest.getDescription());
        assertEquals(request.getRequester().getId(), resultRequest.getRequesterId());
        assertEquals(request.getCreated(), resultRequest.getCreated());
    }

    @Test
    public void testGetAllByRequesterIdNotFound() {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> requestService.getAllByRequesterId(1L));
    }

    @Test
    public void testGetAllByRequesterId() {
        List<ItemRequest> itemRequests = List.of(request);
        List<ItemShort> itemShorts = List.of(
                new ItemShort(1L, "Name", "Description", 1L, true));
        List<ItemRequestGetDto> requestGetDtos = List.of(
                ItemRequestMapper.mapToItemRequestGetDto(itemRequests.get(0), itemShorts));

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(requestRepository.findAllByRequester_IdOrderByCreatedAsc(1L)).thenReturn(itemRequests);
        Mockito.when(itemRepository.findAllByRequestId(1L)).thenReturn(itemShorts);

        List<ItemRequestGetDto> resultRequests = requestService.getAllByRequesterId(user.getId());

        assertEquals(1, resultRequests.size());
        assertEquals(requestGetDtos.get(0), resultRequests.get(0));
    }

    @Test
    public void testGetByIdNotFound() {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> requestService.getById(1L, 1L));

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(requestRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(ItemRequestNotFoundException.class, () -> requestService.getById(1L, 1L));
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
