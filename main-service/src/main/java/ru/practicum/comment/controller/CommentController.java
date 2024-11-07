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
@RequestMapping(path = "/users/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long eventId,
            @Valid @RequestBody CommentDto commentDto) {

        CommentDto createdComment = commentService.addComment(eventId, commentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<CommentDto>> getCommentsByUser(@PathVariable Long userId) {
        List<CommentDto> commentDtos = commentService.getCommentsByUser(userId);
        return ResponseEntity.ok(commentDtos);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestBody CommentDto commentDto) {

        commentService.deleteComment(commentId, commentDto.getAuthorId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<List<CommentDto>> getCommentsByEvent(@PathVariable Long eventId) {
        List<CommentDto> commentDtos = commentService.getCommentsByEvent(eventId);
        return ResponseEntity.ok(commentDtos);
    }
}