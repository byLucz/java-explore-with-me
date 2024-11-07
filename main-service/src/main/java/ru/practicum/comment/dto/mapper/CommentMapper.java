package ru.practicum.comment.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "event.id", target = "eventId")
    CommentDto commentToCommentDto(Comment comment);

    @Mapping(target = "author", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "created", ignore = true)
    Comment commentDtoToComment(CommentDto commentDto);
}

