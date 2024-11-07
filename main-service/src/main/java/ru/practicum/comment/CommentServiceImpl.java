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
    public CommentDto addComment(Long eventId, Long userId, String text) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        CommentDto commentDto = new CommentDto();
        commentDto.setText(text);
        commentDto.setAuthorId(userId);
        commentDto.setEventId(eventId);

        Comment comment = commentMapper.commentDtoToComment(commentDto);
        comment.setAuthor(user);
        comment.setEvent(event);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.commentToCommentDto(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByEvent(Long eventId) {
        List<Comment> comments = commentRepository.findByEventId(eventId);
        return comments.stream()
                .map(commentMapper::commentToCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Comment> comments = commentRepository.findByAuthorId(userId);
        return comments.stream()
                .map(commentMapper::commentToCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IntegrityViolationException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }
}

