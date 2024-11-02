package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.request.ParticipationRequest;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Integer> {

    List<ParticipationRequest> findAllByIdIn(List<Integer> requestIds);

    List<ParticipationRequest> findAllByRequesterId(int requesterId);

    List<ParticipationRequest> findAllByEventId(int eventId);

    ParticipationRequest findOneByEventIdAndRequesterId(int eventId, int requesterId);
}
