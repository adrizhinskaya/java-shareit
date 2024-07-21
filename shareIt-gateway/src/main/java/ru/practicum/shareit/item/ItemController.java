package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ColoredCRUDLogger;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.model.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@Validated
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @Valid @RequestBody ItemDto itemDto) {
        ColoredCRUDLogger.logPost("GATEWAY /items", itemDto.toString());
        return itemClient.create(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long itemId,
                                             @Valid @RequestBody CommentDto comDto) {
        String url = String.format("GATEWAY /items/{%s}/comment", itemId);
        ColoredCRUDLogger.logPost(url, comDto.toString());
        return itemClient.addComment(userId, itemId, comDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestParam(name = "from", defaultValue = "0")
                                                 @PositiveOrZero int from,
                                                 @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        String url = String.format("GATEWAY /items?from{%s}&size{%s}", from, size);
        ColoredCRUDLogger.logGet(url, userId.toString());
        return itemClient.getAllByOwnerId(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getFromSearch(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam String text,
                                                @RequestParam(name = "from", defaultValue = "0")
                                                @PositiveOrZero int from,
                                                @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        String url = String.format("GATEWAY /items/search?from{%s}&size{%s}", from, size);
        ColoredCRUDLogger.logGet(url, text);
        return itemClient.getFromSearch(userId, text, from, size);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable Long itemId) {
        String url = String.format("GATEWAY /items/{%s}", itemId);
        ColoredCRUDLogger.logGet(url, userId.toString());
        return itemClient.getById(userId, itemId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long itemId,
                                         @RequestBody ItemDto itemDto) {
        String url = String.format("GATEWAY /items/{%s}", itemId);
        ColoredCRUDLogger.logPatch(url, itemDto.toString());
        return itemClient.update(userId, itemId, itemDto);
    }
}