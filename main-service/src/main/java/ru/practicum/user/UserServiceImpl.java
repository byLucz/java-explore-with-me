package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.exception.types.IntegrityViolationException;
import ru.practicum.exception.types.NotFoundException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserRequestDto;
import ru.practicum.user.dto.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers(List<Long> ids, int from, int size) {
        log.info("Начало процесса поиска всех пользователей");
        PageRequest pageRequest = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id"));
        List<User> users;

        if (CollectionUtils.isEmpty(ids)) {
            users = userRepository.findAll(pageRequest).getContent();
        } else {
            users = userRepository.findAllByIdIn(ids, pageRequest).getContent();
        }

        log.info("Пользователи найдены");
        return userMapper.listUserToListUserDto(users);
    }

    @Override
    public UserDto createUser(UserRequestDto requestDto) {
        log.info("Начало процесса создания пользователя");
        User user = userMapper.userRequestDtoToUser(requestDto);
        userRepository.findUserByEmail(user.getEmail()).ifPresent(u -> {
            throw new IntegrityViolationException("Пользователь с email " + u.getEmail() + " уже существует");
        });
        userRepository.save(user);
        log.info("Пользователь создан");
        return userMapper.userToUserDto(user);
    }

    @Override
    public void deleteUser(long userId) {
        log.info("Начало процесса удаления пользователя с ID {}", userId);
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(
                "Пользователь с ID = " + userId + " не найден"));
        userRepository.deleteById(userId);
        log.info("Пользователь удален");
    }
}
