package org.example.request.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.example.item.model.ItemShort;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ItemRequestGetDto {
    private Long id;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime created;
    private List<ItemShort> items;
}
