package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestGetDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.UserDto;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
class ItemRequestServiceImplTest {
    private final EntityManager em;
    private final UserService userService;
    private final ItemRequestService requestService;

    @Test
    public void getAllRequests() {
        userService.create(makeUserDto("Пётр", "some@email.com"));
        userService.create(makeUserDto("НеПётр", "any@email.com"));

        Collection<ItemRequestGetDto> itemReqColl = requestService.getAll(1L, 0, 10);
        assertThat(itemReqColl.size(), equalTo(0));

        ItemRequestDto request1 = requestService.create(1L, makeItemRequestDto("Хотел бы воспользоваться щёткой для обуви", LocalDateTime.now()));
        ItemRequestDto request2 = requestService.create(1L, makeItemRequestDto("НЕХотел бы воспользоваться щёткой для обуви", LocalDateTime.now()));

        itemReqColl = requestService.getAll(1L, 0, 10);
        assertThat(itemReqColl.size(), equalTo(0));

        itemReqColl = requestService.getAll(2L, 0, 20);
        assertThat(itemReqColl.size(), equalTo(2));

        itemReqColl = requestService.getAll(2L, 1, 1);
        ItemRequestGetDto itemReq = itemReqColl.iterator().next();
        assertThat(itemReqColl.size(), equalTo(1));
        assertThat(itemReq.getId(), equalTo(request2.getId()));
        assertThat(itemReq.getDescription(), equalTo(request2.getDescription()));
        assertThat(itemReq.getCreated(), equalTo(request2.getCreated()));
        assertThat(itemReq.getItems().size(), equalTo(0));

        assertThrows(UserNotFoundException.class, () -> {
            requestService.getAll(99L, 1, 1);
        });
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