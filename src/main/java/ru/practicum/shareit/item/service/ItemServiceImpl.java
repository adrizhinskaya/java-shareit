package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemBadRequestException;
import ru.practicum.shareit.exception.UserBadRequestException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserBadRequestException("Попытка создания айтема от несуществующего пользователя"));
        Item item = itemRepository.save(ItemMapper.mapToItem(itemDto, user));
        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public Collection<ItemDto> getAll(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserBadRequestException("Попытка просмотра айтемов от несуществующего пользователя"));
        List<Item> items = itemRepository.findAll();
        return ItemMapper.mapToItemDto(items);
    }

    @Override
    public Collection<ItemDto> getFromSearch(Long userId, String text) {
        if (text.isBlank()) return Collections.emptyList();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserBadRequestException("Попытка просмотра айтемов от несуществующего пользователя"));
        List<Item> items = itemRepository.search(text);
        return ItemMapper.mapToItemDto(items);
    }

    @Override
    public ItemDto getById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemBadRequestException("Вещь не найдена"));
        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto newItem) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserBadRequestException("Попытка обновления айтема от несуществующего пользователя"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemBadRequestException("Попытка обновления несуществующей вещи"));
        if (!userId.equals(item.getUserId())) {
            throw new UserBadRequestException("Вещь может обновить только владелец");
        }
        return create(userId, newItem);
    }
}
