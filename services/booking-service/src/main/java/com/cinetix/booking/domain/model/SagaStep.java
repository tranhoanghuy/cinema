package com.cinetix.booking.domain.model;

public enum SagaStep {
    HOLD_SEATS, VALIDATE_VOUCHER, INITIATE_PAYMENT,
    AWAIT_PAYMENT, CONFIRM_SEATS, COMPLETED,
    COMPENSATING, COMPENSATED
}
