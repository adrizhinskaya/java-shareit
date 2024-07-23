package org.example.item.comment;

import org.example.item.ItemRepository;
import org.example.item.model.Item;
import org.example.user.UserRepository;
import org.example.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class CommentRepositoryTest {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    private User user;
    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
        user = makeUser();
        item1 = makeItem("Отвёртка");
        item2 = makeItem("Супер Отвёртка");
        userRepository.save(user);
        itemRepository.save(item1);
        itemRepository.save(item2);
    }

    @Test
    void testFindAllByItem_Id() {
        List<Comment> comments = commentRepository.findAllByItemId(item1.getId());
        assertEquals(0, comments.size());

        Comment comment1 = commentRepository.save(makeComment(item1));
        Comment comment2 = commentRepository.save(makeComment(item2));
        comments = commentRepository.findAllByItemId(item1.getId());
        assertEquals(1, comments.size());
        assertEquals(comment1, comments.get(0));

        Comment comment3 = commentRepository.save(makeComment(item2));
        comments = commentRepository.findAllByItemId(item2.getId());
        assertEquals(2, comments.size());
        assertEquals(comment2, comments.get(0));
    }

    @Test
    void testFindAllByItemIdIn() {
        List<Comment> comments = commentRepository.findAllByItemIdIn(Set.of(item1.getId()));
        assertEquals(0, comments.size());

        Comment comment1 = commentRepository.save(makeComment(item1));
        Comment comment2 = commentRepository.save(makeComment(item2));
        comments = commentRepository.findAllByItemIdIn(Set.of(item1.getId()));
        assertEquals(1, comments.size());
        assertEquals(comment1, comments.get(0));

        Comment comment3 = commentRepository.save(makeComment(item2));
        comments = commentRepository.findAllByItemIdIn(Set.of(item1.getId(), item2.getId()));
        assertEquals(3, comments.size());
        assertEquals(comment1, comments.get(0));
    }

    private Comment makeComment(Item item) {
        return Comment.builder()
                .text("Comment")
                .item(item)
                .author(user)
                .created(LocalDateTime.now())
                .build();
    }

    private Item makeItem(String name) {
        return Item.builder()
                .name(name)
                .description("Аккумуляторная отвертка")
                .available(true)
                .owner(user)
                .request(null)
                .build();
    }

    private User makeUser() {
        return User.builder()
                .name("Пётр")
                .email("some@email.com")
                .build();
    }
}