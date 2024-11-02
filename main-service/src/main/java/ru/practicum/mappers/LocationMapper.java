package ru.practicum.mappers;

import ru.practicum.model.event.Location;
import ru.practicum.model.event.dto.LocationDto;

public class LocationMapper {
    public static Location toLocation(LocationDto locationDto) {
        return new Location(
                locationDto.getId() != null ? locationDto.getId() : 0,
                locationDto.getLat(),
                locationDto.getLon()
        );
    }

    public static LocationDto toLocationDto(Location location) {
        return new LocationDto(
                location.getId(),
                location.getLat(),
                location.getLon()
        );
    }
}
