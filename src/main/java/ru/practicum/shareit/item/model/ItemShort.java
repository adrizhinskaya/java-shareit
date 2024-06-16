package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@AllArgsConstructor
public class ItemShort {
    private Long id;
    private String name;
    private String description;
    private Long requestId;
    private Boolean available;
}
