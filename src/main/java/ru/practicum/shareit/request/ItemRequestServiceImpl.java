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
import ru.practicum.shareit.item.model.ItemShort;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestGetDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


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

        List<ItemRequest> requests = requestRepository.findAllByRequester_IdOrderByCreatedDesc(requesterId);
        Set<Long> requestIds = requests.stream().map(ItemRequest::getId).collect(Collectors.toSet());
        Map<Long, List<ItemShort>> itemsByRequestId = itemRepository.findAllByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(ItemShort::getRequestId));

        return requests.stream()
                .map(request -> ItemRequestMapper
                        .mapToItemRequestGetDto(request, itemsByRequestId.get(request.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestGetDto> getAll(Long requesterId, int from, int size) {
        userExistCheck(requesterId);

        List<ItemRequest> requests = findRequestsByRequesterIdNotWithPagination(requesterId, from, size);
        Set<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toSet());

        Map<Long, List<ItemShort>> itemsByRequestId = itemRepository.findAllByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(ItemShort::getRequestId));

        return requests.stream()
                .map(request -> ItemRequestMapper
                        .mapToItemRequestGetDto(request, itemsByRequestId.get(request.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestGetDto getById(Long userId, Long requestId) {
        userExistCheck(userId);
        ItemRequest request = requestRepository.findById(requestId).orElseThrow(() ->
                new ItemRequestNotFoundException("Запроса " + requestId + " не существует"));
        return ItemRequestMapper.mapToItemRequestGetDto(request, itemRepository.findAllByRequestId(request.getId()));
    }

    private List<ItemRequest> findRequestsByRequesterIdNotWithPagination(Long requesterId, int from, int size) {
        int startPage = from / size;
        Sort sortById = Sort.by(Sort.Direction.DESC, "created");
        Pageable page = PageRequest.of(from / size, size, sortById);
        if (from % size == 0) {
            return requestRepository.findAllByRequester_IdNot(requesterId, page).getContent();
        }

        double nextPageElPercent = (double) from / size - startPage;
        int countOfNextPageEl = (int) Math.ceil(nextPageElPercent * size);
        int countOfStartPageEl = size - countOfNextPageEl;

        page = PageRequest.of(startPage, size, sortById);
        Page<ItemRequest> requestsPage = requestRepository.findAllByRequester_IdNot(requesterId, page);
        List<ItemRequest> requestsList = requestsPage.getContent();
        List<ItemRequest> startPageRequests = requestsList.subList(requestsList.size() - countOfStartPageEl,
                requestsList.size() - 1);

        if (requestsPage.hasNext()) {
            page = PageRequest.of(startPage + 1, size, sortById);
            requestsPage = requestRepository.findAllByRequester_IdNot(requesterId, page);
            List<ItemRequest> nextPageRequests = requestsPage.getContent().subList(0, countOfNextPageEl - 1);
            startPageRequests.addAll(nextPageRequests);
        }

        return startPageRequests;
    }
}