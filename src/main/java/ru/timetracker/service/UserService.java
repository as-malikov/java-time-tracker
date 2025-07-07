package ru.timetracker.service;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TaskRepository taskRepository;
    private final TimeEntryRepository timeEntryRepository;

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        return userMapper.toDTO(getUserEntity(id));
    }

    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        if (userRepository.existsByEmail(userCreateDTO.getEmail())) {
            throw new EmailAlreadyExistsException(userCreateDTO.getEmail());
        }

        User user = userMapper.toEntity(userCreateDTO);
        user = userRepository.save(user);
        return userMapper.toDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        User user = getUserEntity(id);

        if (!user.getEmail().equals(userUpdateDTO.getEmail()) &&
                userRepository.existsByEmail(userUpdateDTO.getEmail())) {
            throw new EmailAlreadyExistsException(userUpdateDTO.getEmail());
        }

        userMapper.updateEntity(userUpdateDTO, user);
        user = userRepository.save(user);
        return userMapper.toDTO(user);
    }

//    @Transactional
//    public void deleteUser(Long id) {
//        User user = getUserEntity(id);
//        userRepository.delete(user);
//    }

    @Transactional(readOnly = true)
    private User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public void deleteUserCompletely(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        timeEntryRepository.deleteByUser(user);
        taskRepository.deleteByUser(user);
        userRepository.delete(user);
    }
}
