package ru.practicum.model.event;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.type.NumericBooleanConverter;
import ru.practicum.model.category.Category;
import ru.practicum.model.user.User;
import ru.practicum.enums.EventStates;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    @Id
    @Column(name = "eventId", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false, length = 2000)
    private String annotation;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "categoryId")
    private Category category;
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    @Column(nullable = false, length = 7000)
    private String description;
    @Column(name = "eventDate", nullable = false)
    private LocalDateTime eventDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "initiatorId")
    private User initiator;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "locationId")
    private Location location;
    @Column(nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean paid;
    @Column(name = "limits", nullable = false)
    private int participantLimit;
    @Column(name = "modRequests", nullable = false)
    private Integer confirmedRequests;
    @Column(name = "published")
    private LocalDateTime published;
    @Column(name = "unmodRequests", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean requestModeration;
    @Enumerated(EnumType.ORDINAL)
    private EventStates state;
    @Column(nullable = false)
    private String title;
}
