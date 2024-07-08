package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestGetDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository requestRepository;

    private User userExistCheck(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException("Для операций c запросами нужно создать пользователя"));
    }

    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto) {
        User user = userExistCheck(userId);
        ItemRequest request = requestRepository.save(ItemRequestMapper.mapToItemRequest(itemRequestDto, user));
        return ItemRequestMapper.mapToItemRequestDto(request);
    }

    @Override
    public List<ItemRequestGetDto> getAllByRequesterId(Long requesterId) {
        userExistCheck(requesterId);
        List<ItemRequest> requests = requestRepository.findAllByRequester_IdOrderByCreatedAsc(requesterId);
        List<ItemRequestGetDto> requestsWithItems = new ArrayList<>(requests.size());
        requests.forEach(request -> requestsWithItems.add(ItemRequestMapper.mapToItemRequestGetDto(
                request,
                itemRepository.findAllByRequestId(request.getId())
        )));
        return requestsWithItems;
    }

    @Override
    public List<ItemRequestGetDto> getAll(Long userId, int from, int size) {
        userExistCheck(userId);
        Sort sortById = Sort.by(Sort.Direction.ASC, "created");
        Pageable page = PageRequest.of(from / size, size, sortById);
        Page<ItemRequest> requests = requestRepository.findAllByRequester_IdNot(userId, page);
        List<ItemRequestGetDto> requestsWithItems = new ArrayList<>(requests.getSize());
        requests.forEach(request -> requestsWithItems.add(ItemRequestMapper.mapToItemRequestGetDto(
                request,
                itemRepository.findAllByRequestId(request.getId())
        )));
        return requestsWithItems;
    }

    @Override
    public ItemRequestGetDto getById(Long userId, Long requestId) {
        userExistCheck(userId);
        ItemRequest request = requestRepository.findById(requestId).orElseThrow(() ->
                new ItemRequestNotFoundException("Запроса " + requestId + " не существует"));
        return ItemRequestMapper.mapToItemRequestGetDto(request, itemRepository.findAllByRequestId(request.getId()));
    }
}