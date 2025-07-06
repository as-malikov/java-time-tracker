package ru.timetracker.service;

import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.timetracker.dto.Mapper.TaskMapper;
import ru.timetracker.dto.TaskRequestDto;
import ru.timetracker.dto.TaskResponseDto;
import ru.timetracker.exception.EntityNotFoundException;
import ru.timetracker.model.Task;
import ru.timetracker.repository.TaskRepository;
import ru.timetracker.repository.UserRepository;
import ru.timetracker.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Data
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserRepository userRepository;

    @Transactional
    public TaskResponseDto createTask(TaskRequestDto taskRequestDto) {
        User user = userRepository.findById(taskRequestDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + taskRequestDto.getUserId()));

        Task task = taskMapper.toEntity(taskRequestDto);
        task.setUser(user);

        Task savedTask = taskRepository.save(task);
        return taskMapper.toDto(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDto> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDto> getUserTasks(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        return taskRepository.findByUserId(userId).stream()
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));
        return taskMapper.toDto(task);
    }

    public TaskResponseDto updateTask(Long id, TaskRequestDto requestDto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        taskMapper.updateEntity(requestDto, task);

        if (!task.getUser().getId().equals(requestDto.getUserId())) {
            User newUser = userRepository.findById(requestDto.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + requestDto.getUserId()));
            task.setUser(newUser);
        }

        Task updatedTask = taskRepository.save(task);
        return taskMapper.toDto(updatedTask);
    }

    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new EntityNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }
}
