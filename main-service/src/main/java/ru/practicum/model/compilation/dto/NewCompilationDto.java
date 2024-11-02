package ru.practicum.model.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class NewCompilationDto {
    private Integer id;
    private List<Integer> events;
    private Boolean pinned;
    @Size(min = 1, max = 50)
    private String title;
}
