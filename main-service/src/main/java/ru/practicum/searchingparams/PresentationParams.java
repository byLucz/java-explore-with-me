package ru.practicum.searchingparams;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.enums.SortTypes;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PresentationParams {
    protected SortTypes sort;
    protected Integer from;
    protected Integer size;
}
