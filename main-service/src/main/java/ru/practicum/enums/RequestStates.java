package ru.practicum.enums;

import java.util.Optional;

public enum RequestStates {
    PENDING, CONFIRMED, REJECTED, CANCELED;

    public static Optional<RequestStates> from(String stringStatus) {
        return EnumUtil.fromString(RequestStates.class, stringStatus);
    }
}
