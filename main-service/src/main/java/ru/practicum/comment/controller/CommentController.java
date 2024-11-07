package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.CommentService;
import ru.practicum.comment.dto.CommentDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody CommentDto commentDto) {

        CommentDto createdComment = commentService.addComment(eventId, userId, commentDto.getText());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @GetMapping
    public ResponseEntity<List<CommentDto>> getCommentsByUser(@PathVariable Long userId) {
        List<CommentDto> commentDtos = commentService.getCommentsByUser(userId);
        return ResponseEntity.ok(commentDtos);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long userId,
            @PathVariable Long commentId) {

        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<List<CommentDto>> getCommentsByEvent(@PathVariable Long eventId) {
        List<CommentDto> commentDtos = commentService.getCommentsByEvent(eventId);
        return ResponseEntity.ok(commentDtos);
    }
}
