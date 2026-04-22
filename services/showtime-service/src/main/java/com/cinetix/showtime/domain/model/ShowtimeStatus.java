package com.cinetix.showtime.domain.model;

public enum ShowtimeStatus {
    SCHEDULED, ON_SALE, FULL, CANCELLED, COMPLETED;

    public boolean isSellable() {
        return this == ON_SALE;
    }
}
