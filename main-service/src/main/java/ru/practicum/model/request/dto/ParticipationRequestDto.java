package ru.practicum.model.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationRequestDto {
    private Integer id;
    @Positive
    private int requester;
    private String created;
    @Positive
    private int event;
    @NotBlank
    private String status;
}
