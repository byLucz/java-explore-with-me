package ru.practicum.model.event;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "LOCATIONS", schema = "PUBLIC")
public class Location {
    @Id
    @Column(name = "LOCATION_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false)
    private double lat;
    @Column(nullable = false)
    private double lon;
}
