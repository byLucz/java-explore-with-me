package ru.practicum.model.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserShortDto {
    private Integer id;
    @NotBlank
    @Size(min = 5)
    private String name;
}
