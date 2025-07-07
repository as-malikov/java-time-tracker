package ru.timetracker.service;

import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.timetracker.dto.Mapper.TaskMapper;
import ru.timetracker.dto.TaskCreateDTO;
import ru.timetracker.dto.TaskDTO;
import ru.timetracker.dto.TaskUpdateDTO;
import ru.timetracker.exception.ResourceNotFoundException;
import ru.timetracker.exception.TaskAlreadyExistsException;
import ru.timetracker.model.Task;
import ru.timetracker.model.User;
import ru.timetracker.repository.TaskRepository;
import ru.timetracker.repository.UserRepository;

import java.util.List;

@Service
@Data
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    @Transactional(readOnly = true)
    public List<TaskDTO> getUserTasks(Long userId, boolean includeInactive) {
        return taskRepository.findByUserId(userId, includeInactive).stream()
                .map(taskMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskDTO getTaskById(Long userId, Long taskId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found with id: " + taskId + " for user: " + userId));
        return taskMapper.toDTO(task);
    }

    @Transactional
    public TaskDTO createTask(Long userId, TaskCreateDTO taskCreateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (taskRepository.existsByUserIdAndTitle(userId, taskCreateDTO.getTitle())) {
            throw new TaskAlreadyExistsException(taskCreateDTO.getTitle());
        }

        Task task = taskMapper.toEntity(taskCreateDTO);
        task.setUser(user);
        task = taskRepository.save(task);
        return taskMapper.toDTO(task);
    }

    @Transactional
    public TaskDTO updateTask(Long taskId, Long userId, TaskUpdateDTO taskUpdateDTO) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found with id: " + taskId + " for user: " + userId));

        taskMapper.updateEntity(taskUpdateDTO, task);

        // Обновляем createdAt только если оно явно указано
        if (taskUpdateDTO.getCreatedAt() != null) {
            task.setCreatedAt(taskUpdateDTO.getCreatedAt());
        }

        task = taskRepository.save(task);
        return taskMapper.toDTO(task);
    }

    @Transactional
    public TaskDTO toggleTaskStatus(Long taskId, Long userId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found with id: " + taskId + " for user: " + userId));

        task.setActive(!task.isActive());
        task = taskRepository.save(task);
        return taskMapper.toDTO(task);
    }

    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found with id: " + taskId + " for user: " + userId));
        taskRepository.delete(task);
    }
}
