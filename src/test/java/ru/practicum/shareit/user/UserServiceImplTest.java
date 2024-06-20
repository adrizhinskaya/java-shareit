package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.UserDto;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Rollback(false)
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
class UserServiceImplTest {

    private final EntityManager em;
    private final UserService userService;

    @Test
    public void getAllUsers() {
        Collection<UserDto> itemReqColl = userService.getAll();
        assertThat(itemReqColl.size(), equalTo(0));

        UserDto user1 = userService.create(makeUserDto("Пётр", "some@email.com"));
        UserDto user2 = userService.create(makeUserDto("НеПётр", "any@email.com"));

        List<UserDto> itemReqList = (List<UserDto>) userService.getAll();
        assertThat(itemReqList.size(), equalTo(2));
        assertThat(itemReqList.get(0), equalTo(user1));
        assertThat(itemReqList.get(1), equalTo(user2));
    }

    private UserDto makeUserDto(String name, String email) {
        return UserDto.builder()
                .name(name)
                .email(email)
                .build();
    }
}