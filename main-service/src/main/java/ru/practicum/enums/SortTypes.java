package ru.practicum.enums;

import java.util.Optional;

public enum SortTypes {
    EVENT_DATE, VIEWS;

    public static Optional<SortTypes> from(String stringStateAction) {
        return EnumUtil.fromString(SortTypes.class, stringStateAction);
    }
}
