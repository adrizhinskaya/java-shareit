package ru.practicum.shareit.item.comment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {
    public static CommentDto mapToCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    public static Collection<CommentDto> mapToCommentDto(Iterable<Comment> comments) {
        Collection<CommentDto> dtos = new ArrayList<>();
        for (Comment comment : comments) {
            dtos.add(mapToCommentDto(comment));
        }
        return dtos;
    }

    public static Comment mapToComment(CommentDto commentDto, User author, Item item) {
        return Comment.builder()
                .id(commentDto.getId())
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(commentDto.getCreated())
                .build();
    }
}
