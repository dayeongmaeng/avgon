package com.avgon.notification.domain;

public enum DeliveryStatus {
    PENDING,
    PROCESSING,
    ACCEPTED,
    RETRYABLE_FAILURE,
    PERMANENT_FAILURE
}
