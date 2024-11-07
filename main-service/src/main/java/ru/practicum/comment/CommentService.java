package ru.practicum.comment;

import ru.practicum.comment.dto.CommentDto;

import java.util.List;

public interface CommentService {
    CommentDto addComment(Long eventId, Long userId, String text);
    List<CommentDto> getCommentsByEvent(Long eventId);
    List<CommentDto> getCommentsByUser(Long userId);
    void deleteComment(Long commentId, Long userId);
}