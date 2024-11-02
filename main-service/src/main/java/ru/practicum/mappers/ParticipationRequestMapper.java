package ru.practicum.mappers;

import ru.practicum.model.request.dto.ParticipationRequestDto;
import ru.practicum.model.request.ParticipationRequest;

public class ParticipationRequestMapper {

    public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest) {
        return new ParticipationRequestDto(
                participationRequest.getId(),
                participationRequest.getRequester().getId(),
                participationRequest.getCreated().toString().substring(0, 24),
                participationRequest.getEvent().getId(),
                participationRequest.getStatus().toString()
        );
    }
}
