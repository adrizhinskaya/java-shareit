package org.example.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.example.exception.ColoredCRUDLogger;
import org.example.request.model.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@Validated
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestClient requestClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @Valid @RequestBody ItemRequestDto itemRequestDto) {
        ColoredCRUDLogger.logPost("GATEWAY /requests", itemRequestDto.toString());
        return requestClient.create(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByRequesterId(@RequestHeader("X-Sharer-User-Id") Long requesterId) {
        ColoredCRUDLogger.logGet("GATEWAY /requests", requesterId.toString());
        return requestClient.getAllByRequesterId(requesterId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
                                         @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        String url = String.format("GATEWAY /requests/all?from={%s}&size={%s}", from, size);
        ColoredCRUDLogger.logGet(url, userId.toString());
        return requestClient.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable Long requestId) {
        String url = String.format("GATEWAY /requests/{%s}", requestId);
        ColoredCRUDLogger.logGet(url, userId.toString());
        return requestClient.getById(userId, requestId);
    }
}
