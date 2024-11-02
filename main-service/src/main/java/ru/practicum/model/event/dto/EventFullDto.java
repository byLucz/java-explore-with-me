package ru.practicum.model.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.user.dto.UserShortDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventFullDto {
    private Integer id;
    private String annotation;
    private CategoryDto category;
    private Integer confirmedRequests;
    private String createdOn;
    private String description;
    private String eventDate;
    private UserShortDto initiator;
    private LocationDto location;
    private Boolean paid;
    private int participantLimit;
    private String publishedOn;
    private Boolean requestModeration;
    private String state;
    private String title;
    private int views;

    public EventFullDto(int id, String annotation, CategoryDto toCategoryDto, Integer confirmedRequests, String format, String description, String format1, UserShortDto toUserShortDto, LocationDto location, Boolean paid, int participantLimit, String format2, Boolean requestModeration, String state, String title, int views) {
    }
}