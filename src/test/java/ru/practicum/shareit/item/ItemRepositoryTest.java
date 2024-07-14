package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemShort;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemRequestRepository requestRepository;
    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        user1 = makeUser("Пётр", "some@email.com");
        user2 = makeUser("НеПётр", "any@email.com");
        request = makeItemRequest();
        userRepository.save(user1);
        userRepository.save(user2);
        requestRepository.save(request);
    }

    @Test
    void testFindByOwnerIdOrderByIdAsc() {
        Pageable page = PageRequest.of(0, 1);
        Page<Item> pages = itemRepository.findByOwnerIdOrderByIdAsc(user1.getId(), page);
        assertEquals(0, pages.getContent().size());

        Item item = itemRepository.save(makeItem("Отвёртка", request));
        pages = itemRepository.findByOwnerIdOrderByIdAsc(user1.getId(), page);
        assertEquals(1, pages.getContent().size());

        Item item2 = itemRepository.save(makeItem("Супер Отвёртка", request));
        pages = itemRepository.findByOwnerIdOrderByIdAsc(user1.getId(), page);
        assertEquals(1, pages.getContent().size());

        page = PageRequest.of(0, 2);
        pages = itemRepository.findByOwnerIdOrderByIdAsc(user1.getId(), page);
        assertEquals(2, pages.getContent().size());
        assertEquals(item, pages.getContent().get(0));
    }

    @Test
    void testFindAllByRequestIdIn() {
        ItemRequest request1 = requestRepository.save(makeItemRequest());
        List<ItemShort> itemShorts = itemRepository.findAllByRequestIdIn(Set.of(request1.getId()));
        assertEquals(0, itemShorts.size());

        Item item = itemRepository.save(makeItem("Отвёртка", request));
        itemShorts = itemRepository.findAllByRequestIdIn(Set.of(request.getId()));
        assertEquals(1, itemShorts.size());

        Item item2 = itemRepository.save(makeItem("Супер Отвёртка", request1));
        itemShorts = itemRepository.findAllByRequestIdIn(Set.of(request.getId(), request1.getId()));
        assertEquals(2, itemShorts.size());
        assertEquals(item.getId(), itemShorts.get(0).getId());
    }

    @Test
    void findAllByRequestId() {
        List<ItemShort> list = itemRepository.findAllByRequestId(1L);
        assertEquals(0, list.size());

        Item item = itemRepository.save(makeItem("Отвёртка", request));
        Item item2 = itemRepository.save(makeItem("Супер Отвёртка", request));
        ItemShort itemShort = makeItemShort(item);
        list = itemRepository.findAllByRequestId(request.getId());
        assertEquals(2, list.size());
        assertEquals(itemShort, list.get(0));
    }

    @Test
    void testSearch() {
        Pageable page = PageRequest.of(0, 1);
        Page<Item> pages = itemRepository.searchTextInNameOrDescription("вёрт", page);
        assertEquals(0, pages.getContent().size());

        Item item = itemRepository.save(makeItem("Отвёртка", request));
        pages = itemRepository.searchTextInNameOrDescription("вёрт", page);
        assertEquals(1, pages.getContent().size());

        Item item2 = itemRepository.save(makeItem("Супер Отвёртка", request));
        pages = itemRepository.searchTextInNameOrDescription("АккуМ", page);
        assertEquals(1, pages.getContent().size());

        page = PageRequest.of(0, 2);
        pages = itemRepository.searchTextInNameOrDescription("АкКум", page);
        assertEquals(2, pages.getContent().size());
        assertEquals(item, pages.getContent().get(0));

        item2.setAvailable(false);
        pages = itemRepository.searchTextInNameOrDescription("ая от", page);
        assertEquals(1, pages.getContent().size());
        assertEquals(item, pages.getContent().get(0));
    }

    private Item makeItem(String name, ItemRequest request) {
        return Item.builder()
                .name(name)
                .description("Аккумуляторная отвертка")
                .available(true)
                .owner(user1)
                .request(request)
                .build();
    }

    private ItemShort makeItemShort(Item item) {
        return ItemShort.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .requestId(item.getRequest().getId())
                .available(item.getAvailable())
                .build();
    }

    private User makeUser(String name, String email) {
        return User.builder()
                .name(name)
                .email(email)
                .build();
    }

    private ItemRequest makeItemRequest() {
        return ItemRequest.builder()
                .description("Request description")
                .requester(user2)
                .created(LocalDateTime.now())
                .build();
    }
}