package org.example.booking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BookingShort {
    private Long itemId;
    private Long id;
    private Long bookerId;
}
