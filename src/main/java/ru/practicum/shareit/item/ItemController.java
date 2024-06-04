package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        log.info("POST /items: {}", itemDto.toString());
        var result = itemService.create(userId, itemDto);
        log.info("completion POST /items: {}", result.toString());
        return result;
    }

    @GetMapping
    public Collection<ItemDto> getAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /items: {}", userId);
        var result = itemService.getAll(userId);
        log.info("completion GET /items: size {}", result.size());
        return result;
    }

    @GetMapping("/search")
    public Collection<ItemDto> getFromSearch(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam String text) {
        log.info("GET /items/search: {}, {}", userId, text);
        var result = itemService.getFromSearch(userId, text);
        log.info("completion GET /items/search: size {}", result.size());
        return result;
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable Long itemId) {
        log.info("GET /items/{id}: {}", itemId);
        var result = itemService.getById(itemId);
        log.info("completion GET /items/{id}: {}", result.toString());
        return result;
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId,
                          @Valid @RequestBody ItemDto item) {
        log.info("PATCH /items/{userId}: {}, {}, {}", userId, itemId, item.toString());
        var result = itemService.update(userId, itemId, item);
        log.info("completion PATCH /items/{userId}: {}", result.toString());
        return result;
    }
}