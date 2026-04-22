package com.cinetix.booking.domain.port.outbound;

import com.cinetix.common.domain.DomainEvent;
import java.util.List;

public interface EventPublisherPort {
    void publishAll(List<DomainEvent> events);
}
