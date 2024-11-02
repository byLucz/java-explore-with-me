package ru.practicum.enums;

import java.util.Optional;

public enum AdminStates {
    PUBLISH_EVENT, REJECT_EVENT;

    public static Optional<AdminStates> from(String stringStateAction) {
        return EnumUtil.fromString(AdminStates.class, stringStateAction);
    }
}
