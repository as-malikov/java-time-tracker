package ru.timetracker.service;

import lombok.Data;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.timetracker.dto.Mapper.UserMapper;
import ru.timetracker.dto.UserRequestDTO;
import ru.timetracker.dto.UserResponseDTO;
import ru.timetracker.exception.EntityNotFoundException;
import ru.timetracker.model.User;
import ru.timetracker.repository.TaskRepository;
import ru.timetracker.repository.TimeEntryRepository;
import ru.timetracker.repository.UserRepository;


import java.util.List;
import java.util.stream.Collectors;

@Service
@Data
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TimeEntryRepository timeEntryRepository;
    private final TaskRepository taskRepository;



    @Transactional
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        User user = userMapper.toEntity(userRequestDTO);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        userMapper.updateEntity(userRequestDto, existingUser);
        User updatedUser = userRepository.save(existingUser);

        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        timeEntryRepository.deleteByTaskUserId(id);
        taskRepository.deleteByUserId(id);
        userRepository.deleteById(id);
        log.info("User {} and all related data deleted", id);
    }

    @Transactional
    public void clearUserTimeEntryData(Long id) {
        timeEntryRepository.deleteByTaskUserId(id);
        log.info("TimeEntry data cleared for user {}", id);
    }
}
