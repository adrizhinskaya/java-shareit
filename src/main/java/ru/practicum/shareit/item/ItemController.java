package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemGetDto;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;
    String postColor = "\u001b[33m" + "POST";
    String patchColor = "\u001b[35m" + "PATCH";
    String getColor = "\u001b[32m" + "GET";
    String resetColor = "\u001b[0m";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        log.info("{} /items: {}{}", postColor, itemDto.toString(), resetColor);
        var result = itemService.create(userId, itemDto);
        log.info("completion POST /items: {}", result.toString());
        return result;
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody CommentDto comDto) {
        log.info("{} /items/{}/comment: {}, {}{}", postColor, itemId, userId, comDto.toString(), resetColor);
        var result = itemService.addComment(userId, itemId, comDto);
        log.info("completion PATCH /items/{userId}: {}", result.toString());
        return result;
    }

    @GetMapping
    public Collection<ItemGetDto> getAllByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("{} /items: {} {}", getColor, userId, resetColor);
        var result = itemService.getAllByOwnerId(userId);
        log.info("completion GET /items: size {}", result.size());
        return result;
    }

    @GetMapping("/search")
    public Collection<ItemDto> getFromSearch(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam String text) {
        log.info("{} /items/search: {}, {}{}", getColor, userId, text, resetColor);
        var result = itemService.getFromSearch(userId, text);
        log.info("completion GET /items/search: size {}", result.size());
        return result;
    }

    @GetMapping("/{itemId}")
    public ItemGetDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long itemId) {
        log.info("{} /items/{}{}", getColor, itemId, resetColor);
        var result = itemService.getById(userId, itemId);
        log.info("completion GET /items/{id}: {}", result.toString());
        return result;
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("{} /items/{}: {}, {}{}", patchColor, itemId, userId, itemDto.toString(), resetColor);
        var result = itemService.update(userId, itemId, itemDto);
        log.info("completion PATCH /items/{userId}: {}", result.toString());
        return result;
    }
}