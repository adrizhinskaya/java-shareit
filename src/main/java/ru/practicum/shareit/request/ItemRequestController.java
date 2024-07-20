package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ColoredCRUDLogger;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestGetDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@Validated
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @Valid @RequestBody ItemRequestDto itemRequestDto) {
        ColoredCRUDLogger.logPost("/requests", itemRequestDto.toString());
        var result = requestService.create(userId, itemRequestDto);
        ColoredCRUDLogger.logPostComplete("/requests", result.toString());
        return result;
    }

    @GetMapping
    public List<ItemRequestGetDto> getAllByRequesterId(@RequestHeader("X-Sharer-User-Id") Long requesterId) {
        ColoredCRUDLogger.logGet("/requests", requesterId.toString());
        var result = requestService.getAllByRequesterId(requesterId);
        ColoredCRUDLogger.logGetComplete("/requests", "size=" + result.size());
        return result;
    }

    @GetMapping("/all")
    public List<ItemRequestGetDto> getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @RequestParam(defaultValue = "0") @Min(0) int from,
                                          @RequestParam(defaultValue = "10") @Min(1) int size) {
        String url = String.format("/requests/all?from={%s}&size={%s}", from, size);
        ColoredCRUDLogger.logGet(url, userId.toString());
        var result = requestService.getAll(userId, from, size);
        ColoredCRUDLogger.logGetComplete(url, "size=" + result.size());
        return result;
    }

    @GetMapping("/{requestId}")
    public ItemRequestGetDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @PathVariable Long requestId) {
        String url = String.format("/requests/{%s}", requestId);
        ColoredCRUDLogger.logGet(url, userId.toString());
        var result = requestService.getById(userId, requestId);
        ColoredCRUDLogger.logGetComplete(url, result.toString());
        return result;
    }
}
