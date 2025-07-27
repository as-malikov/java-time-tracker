package ru.timetracker.service;

import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.timetracker.dto.mapper.UserMapper;
import ru.timetracker.dto.user.UserCreateDTO;
import ru.timetracker.dto.user.UserDTO;
import ru.timetracker.dto.user.UserUpdateDTO;
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
        logger.debug("Request to fetch all users");
        List<UserDTO> users = userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .toList();
        logger.info("Retrieved {} users", users.size());
        return users;
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        logger.debug("Request to fetch user with ID: {}", id);
        UserDTO userDTO = userMapper.toDTO(getUserEntity(id));
        logger.info("Successfully retrieved user with ID: {}", id);
        return userDTO;
    }

    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        logger.debug("Attempting to create new user with email: {}", userCreateDTO.getEmail());

        if (userRepository.existsByEmail(userCreateDTO.getEmail())) {
            String errorMessage = "User with email " + userCreateDTO.getEmail() + " already exists";
            logger.error(errorMessage);
            throw new EmailAlreadyExistsException(userCreateDTO.getEmail());
        }

        User user = userMapper.toEntity(userCreateDTO);
        user = userRepository.save(user);

        logger.info("Created new user: ID={}, Email={}", user.getId(), user.getEmail());
        return userMapper.toDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        logger.debug("Attempting to update user with ID: {}", id);
        User user = getUserEntity(id);

        if (!user.getEmail().equals(userUpdateDTO.getEmail()) &&
                userRepository.existsByEmail(userUpdateDTO.getEmail())) {
            String errorMessage = "Email " + userUpdateDTO.getEmail() + " is already in use by another user";
            logger.error(errorMessage);
            throw new EmailAlreadyExistsException(userUpdateDTO.getEmail());
        }

        userMapper.updateEntity(userUpdateDTO, user);
        user = userRepository.save(user);

        logger.info("Successfully updated user with ID: {}. New email: {}", id, user.getEmail());
        return userMapper.toDTO(user);
    }

    @Transactional(readOnly = true)
    private User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = "User with ID " + id + " not found";
                    logger.error(errorMessage);
                    return new ResourceNotFoundException(errorMessage);
                });
    }

    @Transactional
    public void deleteUserCompletely(Long userId) {
        logger.debug("Attempting complete deletion of user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    String errorMessage = "User with ID " + userId + " not found";
                    logger.error(errorMessage);
                    return new ResourceNotFoundException(errorMessage);
                });

        timeEntryRepository.deleteByUser(user);
        taskRepository.deleteByUser(user);
        userRepository.delete(user);

        logger.info("User with ID: {} has been completely deleted", userId);
    }
}