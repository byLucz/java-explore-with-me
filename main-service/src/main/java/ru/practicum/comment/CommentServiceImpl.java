package ru.practicum.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.types.IntegrityViolationException;
import ru.practicum.exception.types.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto addComment(Long eventId, CommentDto commentDto) {
        log.info("Добавление комментария пользователем с ID {} к событию с ID {}", commentDto.getAuthorId(), eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Событие с ID {} не найдено", eventId);
                    return new NotFoundException("Событие не найдено");
                });
        User user = userRepository.findById(commentDto.getAuthorId())
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", commentDto.getAuthorId());
                    return new NotFoundException("Пользователь не найден");
                });

        Comment comment = commentMapper.commentDtoToComment(commentDto);
        comment.setAuthor(user);
        comment.setEvent(event);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        log.info("Комментарий с ID {} успешно добавлен", savedComment.getId());
        return commentMapper.commentToCommentDto(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByEvent(Long eventId) {
        log.info("Получение комментариев для события с ID {}", eventId);
        List<Comment> comments = commentRepository.findByEventId(eventId);
        log.info("Найдено {} комментариев для события с ID {}", comments.size(), eventId);
        return comments.stream()
                .map(commentMapper::commentToCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByUser(Long authorId) {
        log.info("Получение комментариев пользователя с ID {}", authorId);
        List<Comment> comments = commentRepository.findByAuthorId(authorId);
        log.info("Найдено {} комментариев от пользователя с ID {}", comments.size(), authorId);
        return comments.stream()
                .map(commentMapper::commentToCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long authorId) {
        log.info("Удаление комментария с ID {} пользователем с ID {}", commentId, authorId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("Комментарий с ID {} не найден", commentId);
                    return new NotFoundException("Комментарий не найден");
                });

        if (!comment.getAuthor().getId().equals(authorId)) {
            log.error("Пользователь с ID {} не может удалить комментарий с ID {}", authorId, commentId);
            throw new IntegrityViolationException("Вы можете удалять только свои комментарии");
        }

        commentRepository.delete(comment);
        log.info("Комментарий с ID {} успешно удален", commentId);
    }
}
