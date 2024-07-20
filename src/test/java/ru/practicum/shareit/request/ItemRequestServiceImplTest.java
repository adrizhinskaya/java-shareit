package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestGetDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.UserDto;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
class ItemRequestServiceImplTest {
    private final EntityManager em;
    private final UserService userService;
    private final ItemRequestService requestService;
    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private ItemRepository itemRepository;
    private final EasyRandom generator = new EasyRandom();

    @Test
    public void getAllRequests() {
        UserDto user1 = userService.create(makeUserDto("1Пётр", "some1@email.com"));
        UserDto user2 = userService.create(makeUserDto("1НеПётр", "any1@email.com"));

        List<ItemRequestGetDto> itemReqList = requestService.getAll(user1.getId(), 0, 10);
        assertThat(itemReqList.size(), equalTo(0));

        ItemRequestDto request1 = requestService.create(user1.getId(),
                makeItemRequestDto("Хотел бы воспользоваться щёткой для обуви", LocalDateTime.now()));
        ItemRequestDto request2 = requestService.create(user1.getId(),
                makeItemRequestDto(
                        "НЕХотел бы воспользоваться щёткой для обуви", LocalDateTime.now().plusHours(3)));

        itemReqList = requestService.getAll(user1.getId(), 0, 10);
        assertThat(itemReqList.size(), equalTo(0));

        itemReqList = requestService.getAll(user2.getId(), 0, 20);
        assertThat(itemReqList.size(), equalTo(2));

        itemReqList = requestService.getAll(user2.getId(), 1, 1);
        ItemRequestGetDto itemReq = itemReqList.get(0);
        assertThat(itemReqList.size(), equalTo(1));
        assertThat(itemReq.getId(), equalTo(request1.getId()));
        assertThat(itemReq.getDescription(), equalTo(request1.getDescription()));
        assertThat(itemReq.getCreated(), equalTo(request1.getCreated()));
        assertThat(itemReq.getItems().size(), equalTo(0));

        assertThrows(UserNotFoundException.class, () -> {
            requestService.getAll(99L, 1, 1);
        });

        Mockito.verify(requestRepository, Mockito.never())
                .findAllByRequester_IdNot(any(Long.class), any(Pageable.class));
        Mockito.verify(itemRepository, Mockito.never())
                .findAllByRequestIdIn(any(Set.class));
    }

    @Test
    public void getAllRequestsPaginationTest() {
        UserDto user1 = userService.create(generator.nextObject(UserDto.class));
        UserDto user2 = userService.create(generator.nextObject(UserDto.class));

        ItemRequestDto request1 = requestService.create(user1.getId(), generator.nextObject(ItemRequestDto.class));
        ItemRequestDto request2 = requestService.create(user1.getId(), generator.nextObject(ItemRequestDto.class));
        ItemRequestDto request3 = requestService.create(user1.getId(), generator.nextObject(ItemRequestDto.class));
        ItemRequestDto request4 = requestService.create(user1.getId(), generator.nextObject(ItemRequestDto.class));
        ItemRequestDto request5 = requestService.create(user1.getId(), generator.nextObject(ItemRequestDto.class));
        ItemRequestDto request6 = requestService.create(user1.getId(), generator.nextObject(ItemRequestDto.class));

        List<ItemRequestGetDto> itemReqList = requestService.getAll(user2.getId(), 3, 3);
        assertThat(itemReqList.size(), equalTo(3));
        assertThat(itemReqList.get(0).getId(), equalTo(request3.getId()));

        itemReqList = requestService.getAll(user2.getId(), 5, 6);
        assertThat(itemReqList.size(), equalTo(1));
        assertThat(itemReqList.get(0).getId(), equalTo(request1.getId()));

        itemReqList = requestService.getAll(user2.getId(), 5, 3);
        assertThat(itemReqList.size(), equalTo(1));
        assertThat(itemReqList.get(0).getId(), equalTo(request1.getId()));

        itemReqList = requestService.getAll(user2.getId(), 2, 3);
        assertThat(itemReqList.size(), equalTo(3));
        assertThat(itemReqList.get(0).getId(), equalTo(request4.getId()));

        itemReqList = requestService.getAll(user2.getId(), 7, 6);
        assertThat(itemReqList.size(), equalTo(0));
    }

    private UserDto makeUserDto(String name, String email) {
        return UserDto.builder()
                .name(name)
                .email(email)
                .build();
    }

    private ItemRequestDto makeItemRequestDto(String description, LocalDateTime created) {
        return ItemRequestDto.builder()
                .description(description)
                .created(created)
                .build();
    }
}