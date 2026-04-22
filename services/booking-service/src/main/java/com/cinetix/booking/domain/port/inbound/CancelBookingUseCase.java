package com.cinetix.booking.domain.port.inbound;

import java.util.UUID;

public interface CancelBookingUseCase {
    void execute(UUID bookingId, UUID requesterId);
}
