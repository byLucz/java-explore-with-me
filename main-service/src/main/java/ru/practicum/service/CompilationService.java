package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.model.compilation.Compilation;
import ru.practicum.model.compilation.dto.CompilationDto;
import ru.practicum.model.compilation.dto.NewCompilationDto;
import ru.practicum.model.event.Event;
import ru.practicum.mappers.CompilationMapper;
import ru.practicum.repository.CompilationRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventService eventService;

    @Transactional
    public CompilationDto saveCompilation(NewCompilationDto newCompilationDto) {
        if (newCompilationDto.getTitle() == null || newCompilationDto.getTitle().isBlank()) {
            throw new IllegalArgumentException("При создании подборки отсутствие заголовка не допускается");
        }
        List<Integer> eventsIds = new ArrayList<>();
        if (newCompilationDto.getEvents() != null) {
            eventsIds = newCompilationDto.getEvents();
        }
        Set<Event> eventsForCompilation = new HashSet<>(eventService.getEventsByIds(eventsIds));
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto, eventsForCompilation);

        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Transactional(readOnly = true)
    public Compilation getCompilation(int compId) {
        return compilationRepository.getReferenceById(compId);
    }

    @Transactional(readOnly = true)
    public CompilationDto getCompilationDtoOut(int compId) {
        return CompilationMapper.toCompilationDto(compilationRepository.getReferenceById(compId));
    }

    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Compilation> compilations;
        compilations = compilationRepository.findByPinnedForPublic(pinned, pageable);
        return compilations.stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCompilation(int compId) {
        Compilation compilation = getCompilation(compId);
        compilation.getId();
        compilationRepository.delete(compilation);
    }

    @Transactional
    public CompilationDto updateCompilation(int compId, NewCompilationDto newCompilationDto) {
        Compilation updatingCompilation = getCompilation(compId);
        newCompilationDto.setId(compId);
        if (newCompilationDto.getTitle() == null) {
            newCompilationDto.setTitle(updatingCompilation.getTitle());
        }
        if (newCompilationDto.getPinned() == null) {
            newCompilationDto.setPinned(updatingCompilation.getPinned());
        }
        if (newCompilationDto.getEvents() == null) {
            newCompilationDto.setEvents(updatingCompilation.getEvents()
                    .stream()
                    .map(Event::getId)
                    .collect(Collectors.toList()));
        }
        return saveCompilation(newCompilationDto);
    }
}
