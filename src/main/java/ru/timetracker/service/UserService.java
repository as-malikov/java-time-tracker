package ru.timetracker.service;

import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.timetracker.dto.Mapper.UserMapper;
import ru.timetracker.dto.UserCreateDTO;
import ru.timetracker.dto.UserDTO;
import ru.timetracker.dto.UserUpdateDTO;
import ru.timetracker.exception.EmailAlreadyExistsException;
import ru.timetracker.exception.ResourceNotFoundException;
import ru.timetracker.model.User;
import ru.timetracker.repository.TaskRepository;
import ru.timetracker.repository.TimeEntryRepository;
import ru.timetracker.repository.UserRepository;

import java.util.List;

@Service
@Data
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TaskRepository taskRepository;
    private final TimeEntryRepository timeEntryRepository;

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        logger.debug("Запрос на получение всех пользователей");
        List<UserDTO> users = userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .toList();
        logger.info("Получено {} пользователей", users.size());
        return users;
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        logger.debug("Запрос на получение пользователя с ID: {}", id);
        UserDTO userDTO = userMapper.toDTO(getUserEntity(id));
        logger.info("Пользователь с ID: {} успешно получен", id);
        return userDTO;
    }

    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        logger.debug("Попытка создания нового пользователя с email: {}", userCreateDTO.getEmail());

        if (userRepository.existsByEmail(userCreateDTO.getEmail())) {
            String errorMessage = "Пользователь с email " + userCreateDTO.getEmail() + " уже существует";
            logger.error(errorMessage);
            throw new EmailAlreadyExistsException(userCreateDTO.getEmail());
        }

        User user = userMapper.toEntity(userCreateDTO);
        user = userRepository.save(user);

        logger.info("Создан новый пользователь: ID={}, Email={}", user.getId(), user.getEmail());
        return userMapper.toDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        logger.debug("Попытка обновления пользователя с ID: {}", id);
        User user = getUserEntity(id);

        if (!user.getEmail().equals(userUpdateDTO.getEmail()) &&
                userRepository.existsByEmail(userUpdateDTO.getEmail())) {
            String errorMessage = "Email " + userUpdateDTO.getEmail() + " уже используется другим пользователем";
            logger.error(errorMessage);
            throw new EmailAlreadyExistsException(userUpdateDTO.getEmail());
        }

        userMapper.updateEntity(userUpdateDTO, user);
        user = userRepository.save(user);

        logger.info("Пользователь с ID: {} успешно обновлен. Новый email: {}", id, user.getEmail());
        return userMapper.toDTO(user);
    }

    @Transactional(readOnly = true)
    private User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = "Пользователь с ID " + id + " не найден";
                    logger.error(errorMessage);
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    @Transactional
    public void deleteUserCompletely(Long userId) {
        logger.debug("Попытка полного удаления пользователя с ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    String errorMessage = "Пользователь с ID " + userId + " не найден";
                    logger.error(errorMessage);
                    return new ResourceNotFoundException(errorMessage);
                });

        timeEntryRepository.deleteByUser(user);
        taskRepository.deleteByUser(user);
        userRepository.delete(user);

        logger.info("Пользователь с ID: {} полностью удален.",
                userId);
    }
}