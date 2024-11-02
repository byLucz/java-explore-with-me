package ru.practicum.enums;

import java.util.Optional;

public enum UserStates {
    SEND_TO_REVIEW, CANCEL_REVIEW;

    public static Optional<UserStates> from(String stringStateAction) {
        return EnumUtil.fromString(UserStates.class, stringStateAction);
    }
}
