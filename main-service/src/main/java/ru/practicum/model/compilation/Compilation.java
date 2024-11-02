package ru.practicum.model.compilation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.type.NumericBooleanConverter;
import ru.practicum.model.event.Event;

import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Compilation {
    @Id
    @Column(name = "compilationId", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean pinned;
    @ManyToMany
    @JoinTable(name = "EventCompilation",
            joinColumns = @JoinColumn(name = "compilationId"),
            inverseJoinColumns = @JoinColumn(name = "eventId"))
    private Set<Event> events;
}
