package ru.practicum.searchingparams;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.enums.EventStates;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AdminParams {
    protected List<Integer> users;
    protected List<EventStates> states;
    protected List<Integer> categories;
    protected LocalDateTime rangeStart;
    protected LocalDateTime rangeEnd;
}
