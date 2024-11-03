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
@Table(name = "EVENTS", schema = "PUBLIC")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    @Id
    @Column(name = "EVENT_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false, length = 2000)
    private String annotation;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;
    @Column(name = "CREATED", nullable = false)
    private LocalDateTime created;
    @Column(nullable = false, length = 7000)
    private String description;
    @Column(name = "EVENT_DATE", nullable = false)
    private LocalDateTime eventDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "INITIATOR_ID")
    private User initiator;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "LOCATION_ID")
    private Location location;
    @Column(nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean paid;
    @Column(name = "PARTICIPANT_LIMIT", nullable = false)
    private int participantLimit;
    @Column(name = "CONFIRMED_REQUESTS", nullable = false)
    private Integer confirmedRequests;
    @Column(name = "PUBLISHED")
    private LocalDateTime published;
    @Column(name = "CHECKIN_REQUESTS", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean checkinRequests;
    @Enumerated(EnumType.ORDINAL)
    private EventStates state;
    @Column(nullable = false)
    private String title;
}
