package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.request.ParticipationRequest;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Integer> {

    ParticipationRequest findOneByEvent_IdAndRequester_Id(int eventId, int requesterId);

    List<ParticipationRequest> findAllByIdIn(List<Integer> requestIds);

    List<ParticipationRequest> findAllByRequester_Id(int requesterId);

    List<ParticipationRequest> findAllByEvent_Id(int eventId);
}
