package ru.practicum.model.request;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.model.event.Event;
import ru.practicum.model.user.User;
import ru.practicum.enums.RequestStates;

import java.time.LocalDateTime;

@Entity
@Table(name = "PARTICIPATION_REQUESTS", schema = "PUBLIC")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ParticipationRequest {
    @Id
    @Column(name = "REQUEST_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false)
    private LocalDateTime created;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "EVENT_ID")
    private Event event;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "REQUESTER_ID")
    private User requester;
    @Enumerated(EnumType.ORDINAL)
    private RequestStates status;
}
