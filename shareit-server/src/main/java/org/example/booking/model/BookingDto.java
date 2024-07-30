package org.example.booking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.booking.BookingStatus;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long itemId;
    private BookingStatus status;

    public boolean isStartDateBeforeEndDate() {
        if (start == null || end == null) {
            return true;
        }
        return start.isBefore(end);
    }
}
