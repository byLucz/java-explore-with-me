package ru.practicum.model.event.dto;

import lombok.*;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.user.dto.UserShortDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EventShortDto {
    private Integer id;
    private String annotation;
    private CategoryDto category;
    private Integer confirmedRequests;
    private String eventDate;
    private UserShortDto initiator;
    private Boolean paid;
    private String title;
    private int views;
}
