package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestGetDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException("Попытка создания запроса от несуществующего пользователя"));
        ItemRequest request = requestRepository.save(ItemRequestMapper.mapToItemRequest(itemRequestDto, user));
        return ItemRequestMapper.mapToItemRequestDto(request);
    }

    @Override
    public Collection<ItemRequestGetDto> getAllByRequesterId(Long requesterId) {
        User user = userRepository.findById(requesterId).orElseThrow(() ->
                new UserNotFoundException("Попытка просмотра запросов от несуществующего пользователя"));

        Collection<ItemRequest> requests = requestRepository.findAllByRequester_IdOrderByCreatedAsc(requesterId);
        Collection<ItemRequestGetDto> requestsWithItems = new ArrayList<>(requests.size());
        requests.forEach(request -> requestsWithItems.add(ItemRequestMapper.mapToItemRequestGetDto(
                request,
                itemRepository.findAllByRequestId(request.getId())
        )));
        return requestsWithItems;
    }

    @Override
    public Collection<ItemRequestDto> getAll(Long userId, int from, int size) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException("Попытка просмотра запросов от несуществующего пользователя"));

        Sort sortById = Sort.by(Sort.Direction.ASC, "created");
        // затем создаём описание первой "страницы" размером 32 элемента !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        Pageable page = PageRequest.of(from, size, sortById);
        return  ItemRequestMapper.mapToItemRequestDto(requestRepository.findAll(page));
    }

    @Override
    public ItemRequestGetDto getById(Long userId, Long requestId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException("Попытка просмотра запросов от несуществующего пользователя"));

        ItemRequest request = requestRepository.findById(requestId).orElseThrow(() ->
                new ItemRequestNotFoundException("Запроса " + requestId + " не существует"));

        return ItemRequestMapper.mapToItemRequestGetDto(request, itemRepository.findAllByRequestId(request.getId()));
    }
}