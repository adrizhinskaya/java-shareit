package org.example.request;

import org.example.request.model.ItemRequest;
import org.example.user.UserRepository;
import org.example.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ItemRequestRepositoryTest {
    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = makeUser();
        userRepository.save(user);
    }

    @Test
    void testFindAllByRequester_IdOrderByCreatedAsc() {
        List<ItemRequest> list = requestRepository.findAllByRequester_IdOrderByCreatedDesc(1L);
        assertEquals(0, list.size());

        ItemRequest itemRequest = requestRepository.save(makeItemRequest(LocalDateTime.now()));
        list = requestRepository.findAllByRequester_IdOrderByCreatedDesc(itemRequest.getRequester().getId());
        assertEquals(1, list.size());

        ItemRequest itemRequest2 = requestRepository.save(makeItemRequest(LocalDateTime.now().plusHours(1)));
        list = requestRepository.findAllByRequester_IdOrderByCreatedDesc(itemRequest.getRequester().getId());
        assertEquals(2, list.size());
        assertEquals(itemRequest2, list.get(0));
    }

    @Test
    void testFindAllByRequester_IdNot() {
        Sort sortById = Sort.by(Sort.Direction.ASC, "created");
        Pageable page = PageRequest.of(0, 1, sortById);
        Page<ItemRequest> pages = requestRepository.findAllByRequester_IdNot(1L, page);
        assertEquals(0, pages.getContent().size());

        ItemRequest itemRequest = requestRepository.save(makeItemRequest(LocalDateTime.now()));
        pages = requestRepository.findAllByRequester_IdNot(2L, page);
        assertEquals(1, pages.getContent().size());

        ItemRequest itemRequest2 = requestRepository.save(makeItemRequest(LocalDateTime.now().plusHours(1)));
        pages = requestRepository.findAllByRequester_IdNot(2L, page);
        assertEquals(1, pages.getContent().size());
        assertEquals(itemRequest, pages.getContent().get(0));

        page = PageRequest.of(0, 2, sortById);
        pages = requestRepository.findAllByRequester_IdNot(2L, page);
        assertEquals(2, pages.getContent().size());
        assertEquals(itemRequest, pages.getContent().get(0));
    }

    private ItemRequest makeItemRequest(LocalDateTime created) {
        return ItemRequest.builder()
                .description("Request description")
                .requester(user)
                .created(created)
                .build();
    }

    private User makeUser() {
        return User.builder()
                .name("Пётр")
                .email("some@email.com")
                .build();
    }
}