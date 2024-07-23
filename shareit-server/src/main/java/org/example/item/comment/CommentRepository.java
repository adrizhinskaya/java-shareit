package org.example.item.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByItemIdIn(Set<Long> itemIds);

    List<Comment> findAllByItemId(Long itemId);
}
