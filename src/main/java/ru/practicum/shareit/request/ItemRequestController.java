package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestGetDto;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService requestService;
    String postColor = "\u001b[33m" + "POST";
    String patchColor = "\u001b[35m" + "PATCH";
    String getColor = "\u001b[32m" + "GET";
    String resetColor = "\u001b[0m";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("{} /requests: {}{}", postColor, itemRequestDto.toString(), resetColor);
        var result = requestService.create(userId, itemRequestDto);
        log.info("completion POST /requests: {}", result.toString());
        return result;
    }

    @GetMapping
    public Collection<ItemRequestGetDto> getAllByRequesterId(@RequestHeader("X-Sharer-User-Id") Long requesterId) {
        log.info("{} /requests: {} {}", getColor, requesterId, resetColor);
        var result = requestService.getAllByRequesterId(requesterId);
        log.info("completion GET /requests: size {}", result.size());
        return result;
    }

    @GetMapping("/all")
    public Collection<ItemRequestDto> getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "10") int size) {
        log.info("{} /requests/all?from={}&size={} {}", getColor, from, size, resetColor);
        var result = requestService.getAll(userId, from, size);
        log.info("completion GET /requests/all?from={}&size={}: size {}", from, size, result.size());
        return result;
    }

    @GetMapping("/{requestId}")
    public ItemRequestGetDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @PathVariable Long requestId) {
        log.info("{} /requests/{} {}", getColor, requestId, resetColor);
        var result = requestService.getById(userId, requestId);
        log.info("completion GET /requests/{}: size {}", requestId, result.toString());
        return result;
    }
}
