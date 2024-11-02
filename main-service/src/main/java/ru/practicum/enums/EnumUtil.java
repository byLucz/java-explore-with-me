package ru.practicum.enums;

import java.util.Optional;

public class EnumUtil {
    public static <T extends Enum<T>> Optional<T> fromString(Class<T> enumClass, String value) {
        for (T enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.name().equalsIgnoreCase(value)) {
                return Optional.of(enumConstant);
            }
        }
        return Optional.empty();
    }
}
