package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ColoredCRUDLogger;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        ColoredCRUDLogger.logPost("/items", itemDto.toString());
        var result = itemService.create(userId, itemDto);
        ColoredCRUDLogger.logPostComplete("/items", result.toString());
        return result;
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody CommentDto comDto) {
        String url = String.format("/items/{%s}/comment", itemId);
        ColoredCRUDLogger.logPost(url, comDto.toString());
        var result = itemService.addComment(userId, itemId, comDto);
        ColoredCRUDLogger.logPostComplete(url, result.toString());
        return result;
    }

    @GetMapping
    public List<ItemGetDto> getAllByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam(defaultValue = "0") @Min(0) int from,
                                           @RequestParam(defaultValue = "10") @Min(1) int size) {
        String url = String.format("/items?from{%s}&size{%s}", from, size);
        ColoredCRUDLogger.logGet(url, userId.toString());
        var result = itemService.getAllByOwnerId(userId, from, size);
        ColoredCRUDLogger.logGetComplete(url, "size=" + result.size());
        return result;
    }

    @GetMapping("/search")
    public List<ItemDto> getFromSearch(@RequestHeader("X-Sharer-User-Id") Long userId,
                                       @RequestParam String text,
                                       @RequestParam(defaultValue = "0") @Min(0) int from,
                                       @RequestParam(defaultValue = "10") @Min(1) int size) {
        String url = String.format("/items/search?from{%s}&size{%s}", from, size);
        ColoredCRUDLogger.logGet(url, text);
        var result = itemService.getFromSearch(userId, text, from, size);
        ColoredCRUDLogger.logGetComplete(url, "size=" + result.size());
        return result;
    }

    @GetMapping("/{itemId}")
    public ItemGetDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long itemId) {
        String url = String.format("/items/{%s}", itemId);
        ColoredCRUDLogger.logGet(url, userId.toString());
        var result = itemService.getById(userId, itemId);
        ColoredCRUDLogger.logGetComplete(url, result.toString());
        return result;
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        String url = String.format("/items/{%s}", itemId);
        ColoredCRUDLogger.logPatch(url, itemDto.toString());
        var result = itemService.update(userId, itemId, itemDto);
        ColoredCRUDLogger.logPatchComplete(url, result.toString());
        return result;
    }
}