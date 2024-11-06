package ru.practicum.user;

import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserRequestDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers(List<Long> ids, int from, int size);

    UserDto createUser(UserRequestDto requestDto);

    void deleteUser(long userId);
}
