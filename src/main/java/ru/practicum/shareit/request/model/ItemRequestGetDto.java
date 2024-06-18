package ru.practicum.shareit.request.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemShort;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Collection;

@Data
@Builder
public class ItemRequestGetDto {
    private Long id;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime created;
    private Collection<ItemShort> items;
}
