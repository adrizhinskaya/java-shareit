package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemBadRequestException;
import ru.practicum.shareit.exception.UserBadRequestException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Item create(Long userId, ItemDto itemDto) {
        if (!userRepository.userExists(userId)) {
            throw new UserBadRequestException("Попытка создания айтема от несуществующего пользователя");
        }
        return itemRepository.create(userId, itemDto);
    }

    @Override
    public Collection<Item> getAll(Long userId) {
        if (!userRepository.userExists(userId)) {
            throw new UserBadRequestException("Попытка просмотра айтемов от несуществующего пользователя");
        }
        return itemRepository.getAll(userId);
    }

    @Override
    public Collection<Item> getFromSearch(Long userId, String text) {
        if (!userRepository.userExists(userId)) {
            throw new UserBadRequestException("Попытка просмотра айтемов от несуществующего пользователя");
        }
        return itemRepository.getFromSearch(text);
    }

    @Override
    public Item getById(Long id) {
        return itemRepository.getById(id);
    }

    @Override
    public Item update(Long userId, Long itemId, Item newItem) {
        Item item = itemRepository.getById(itemId);
        if (!userRepository.userExists(userId)) {
            throw new UserBadRequestException("Попытка обновления айтема от несуществующего пользователя");
        }
        if (!itemRepository.itemExists(itemId)) {
            throw new ItemBadRequestException("Попытка обновления несуществующей вещи");
        }
        if (!userId.equals(item.getOwner().getId())) {
            throw new UserBadRequestException("Вещь может обновить только владелец");
        }
        return itemRepository.update(userId, item, newItem);
    }
}
