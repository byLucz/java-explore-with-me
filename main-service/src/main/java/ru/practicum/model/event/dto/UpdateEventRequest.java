package ru.practicum.model.event.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UpdateEventRequest {
    @Size(min = 20, max = 2000)
    private String annotation;
    private Integer category;
    @Size(min = 20, max = 7000)
    private String description;
    private String eventDate;
    private LocationDto location;
    private Boolean paid;
    @PositiveOrZero
    private Integer participantLimit;
    private Boolean checkinRequests;
    private String stateAction;
    @Size(min = 3, max = 120)
    private String title;
}
