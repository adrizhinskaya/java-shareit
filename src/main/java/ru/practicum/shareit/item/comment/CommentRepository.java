package ru.practicum.shareit.item.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Collection<Comment> findAllByItem_Id(Long itemId);
}
