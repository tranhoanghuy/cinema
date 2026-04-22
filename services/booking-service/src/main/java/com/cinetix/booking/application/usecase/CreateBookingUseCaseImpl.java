package com.cinetix.booking.application.usecase;

import com.cinetix.booking.application.dto.command.CreateBookingCommand;
import com.cinetix.booking.application.saga.BookingSagaOrchestrator;
import com.cinetix.booking.domain.model.valueobject.BookingId;
import com.cinetix.booking.domain.port.inbound.CreateBookingUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateBookingUseCaseImpl implements CreateBookingUseCase {

    private final BookingSagaOrchestrator saga;

    @Override
    public BookingId execute(CreateBookingCommand command) {
        return saga.start(command);
    }
}
