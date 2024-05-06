package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InMemoryItemRepository implements ItemRepository {
    private Long id = 0L;
    private final Map<Long, Item> items = new HashMap<>();
    private final UserRepository userRepository;

    @Override
    public Item create(Long userId, ItemDto itemDto) {
        Item item = Item.builder()
                .id(generateId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(userRepository.getById(userId))
                .build();
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Collection<Item> getAll(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Item> getFromSearch(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return items.values().stream()
                .filter(item -> item.getAvailable().equals(true))
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public Item getById(Long id) {
        return items.getOrDefault(id, null);
    }

    @Override
    public Item update(Long userId, Item item, Item newItem) {
        if (newItem.getName() != null) {
            item.setName(newItem.getName());
        }
        if (newItem.getDescription() != null) {
            item.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null) {
            item.setAvailable(newItem.getAvailable());
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public boolean itemExists(Long id) {
        return items.values().stream()
                .anyMatch(item -> item.getId().equals(id));
    }

    private Long generateId() {
        return ++id;
    }
}
