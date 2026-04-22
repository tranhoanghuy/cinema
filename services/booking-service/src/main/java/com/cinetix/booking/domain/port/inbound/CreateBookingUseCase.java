package com.cinetix.booking.domain.port.inbound;

import com.cinetix.booking.application.dto.command.CreateBookingCommand;
import com.cinetix.booking.domain.model.valueobject.BookingId;

public interface CreateBookingUseCase {
    BookingId execute(CreateBookingCommand command);
}
