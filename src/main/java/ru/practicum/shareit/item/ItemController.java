package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemGetDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@Validated
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
    public List<ItemGetDto> getAllByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam(defaultValue = "0") @Min(0) int from,
                                           @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("{} /items?from{}&size{}: {} {}", getColor, from, size, userId, resetColor);
        var result = itemService.getAllByOwnerId(userId, from, size);
        log.info("completion GET /items?from{}&size{}: size {}", from, size, result.size());
        return result;
    }

    @GetMapping("/search")
    public List<ItemDto> getFromSearch(@RequestHeader("X-Sharer-User-Id") Long userId,
                                       @RequestParam String text,
                                       @RequestParam(defaultValue = "0") @Min(0) int from,
                                       @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("{} /items/search?from{}&size{}: {}, {}{}", getColor, from, size, userId, text, resetColor);
        var result = itemService.getFromSearch(userId, text, from, size);
        log.info("completion GET/items/search?from{}&size{}: size {}", from, size, result.size());
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