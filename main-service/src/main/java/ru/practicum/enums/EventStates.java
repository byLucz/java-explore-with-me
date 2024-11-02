package ru.practicum.enums;

import java.util.Optional;

public enum EventStates {
    PENDING, PUBLISHED, CANCELED;

    public static Optional<EventStates> from(String stringState) {
        return EnumUtil.fromString(EventStates.class, stringState);
    }
}
