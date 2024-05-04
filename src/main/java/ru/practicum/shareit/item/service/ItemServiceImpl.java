package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemBadRequestException;
import ru.practicum.shareit.exception.UserBadRequestException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Item create(Long userId, ItemDto itemDto) {
        log.info("POST /items");
        if (!userRepository.userExists(userId)) {
            throw new UserBadRequestException("Попытка создания айтема от несуществующего пользователя");
        }
        Item result = itemRepository.create(userId, itemDto);
        log.info("Создана вещь {}", result.toString());
        return result;
    }

    @Override
    public Collection<Item> getAll(Long userId) {
        log.info("GET /items");
        if (!userRepository.userExists(userId)) {
            throw new UserBadRequestException("Попытка просмотра айтемов от несуществующего пользователя");
        }
        Collection<Item> result = itemRepository.getAll(userId);
        log.info("Получен список вещей из {} элементов", result.size());
        return result;
    }

    @Override
    public Collection<Item> getFromSearch(Long userId, String text) {
        log.info("GET /items/search");
        if (!userRepository.userExists(userId)) {
            throw new UserBadRequestException("Попытка просмотра айтемов от несуществующего пользователя");
        }
        Collection<Item> result = itemRepository.getFromSearch(text);
        log.info("Получен список вещей из {} элементов", result.size());
        return result;
    }

    @Override
    public Item getById(Long id) {
        log.info("GET /items/{id}");
        Item result = itemRepository.getById(id);
        log.info("Получена вещь {}", result.toString());
        return result;
    }

    @Override
    public Item update(Long userId, Long itemId, Item item) {
        log.info("PATCH /items/{userId}");
        if (!userRepository.userExists(userId)) {
            throw new UserBadRequestException("Попытка обновления айтема от несуществующего пользователя");
        }
        if (!itemRepository.itemExists(itemId)) {
            throw new ItemBadRequestException("Попытка обновления несуществующей вещи");
        }
        if (!userId.equals(itemRepository.getById(itemId).getOwner().getId())) {
            throw new UserBadRequestException("Вещь может обновить только владелец");
        }
        Item result = itemRepository.update(userId, itemId, item);
        log.info("Обновлёна вещь {}", result.toString());
        return result;
    }
}
