package com.cinetix.booking.domain.port.inbound;

import com.cinetix.booking.application.dto.query.BookingDetailDto;
import com.cinetix.booking.domain.model.valueobject.BookingId;
import com.cinetix.common.web.PagedResponse;

import java.util.UUID;

public interface GetBookingUseCase {
    BookingDetailDto findById(BookingId id);
    PagedResponse<BookingDetailDto> findByCustomer(UUID customerId, String status, int page, int size);
}
