package ru.timetracker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.timetracker.dto.mapper.TaskMapper;
import ru.timetracker.dto.task.TaskCreateDTO;
import ru.timetracker.dto.task.TaskDTO;
import ru.timetracker.dto.task.TaskUpdateDTO;
import ru.timetracker.exception.ResourceNotFoundException;
import ru.timetracker.exception.TaskAlreadyExistsException;
import ru.timetracker.model.Task;
import ru.timetracker.model.User;
import ru.timetracker.repository.TaskRepository;
import ru.timetracker.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private final Long userId = 1L;
    private final Long taskId = 1L;

    @Test
    void getUserTasks_ShouldReturnTasks() {
        // Arrange
        Task task = new Task();
        TaskDTO taskDTO = new TaskDTO();
        when(taskRepository.findByUserId(userId, true)).thenReturn(List.of(task));
        when(taskMapper.toDTO(task)).thenReturn(taskDTO);

        // Act
        List<TaskDTO> result = taskService.getUserTasks(userId, true);

        // Assert
        assertEquals(1, result.size());
        assertEquals(taskDTO, result.get(0));
        verify(taskRepository).findByUserId(userId, true);
    }

    @Test
    void getTaskById_ShouldReturnTask_WhenExists() {
        // Arrange
        Task task = new Task();
        TaskDTO taskDTO = new TaskDTO();
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));
        when(taskMapper.toDTO(task)).thenReturn(taskDTO);

        // Act
        TaskDTO result = taskService.getTaskById(userId, taskId);

        // Assert
        assertEquals(taskDTO, result);
        verify(taskRepository).findByIdAndUserId(taskId, userId);
    }

    @Test
    void getTaskById_ShouldThrowException_WhenNotFound() {
        // Arrange
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> taskService.getTaskById(userId, taskId));
    }

    @Test
    void createTask_ShouldCreateNewTask() {
        // Arrange
        TaskCreateDTO createDTO = new TaskCreateDTO("New Task", "Description");
        User user = new User();
        Task task = new Task();
        Task savedTask = new Task();
        TaskDTO taskDTO = new TaskDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.existsByUserIdAndTitle(userId, createDTO.getTitle())).thenReturn(false);
        when(taskMapper.toEntity(createDTO)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(savedTask);
        when(taskMapper.toDTO(savedTask)).thenReturn(taskDTO);

        // Act
        TaskDTO result = taskService.createTask(userId, createDTO);

        // Assert
        assertEquals(taskDTO, result);
        verify(taskRepository).save(task);
        assertEquals(user, task.getUser());
    }

    @Test
    void createTask_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        TaskCreateDTO createDTO = new TaskCreateDTO("New Task", "Description");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> taskService.createTask(userId, createDTO));
    }

    @Test
    void createTask_ShouldThrowException_WhenTitleExists() {
        // Arrange
        TaskCreateDTO createDTO = new TaskCreateDTO("Existing Task", "Description");
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.existsByUserIdAndTitle(userId, createDTO.getTitle())).thenReturn(true);

        // Act & Assert
        assertThrows(TaskAlreadyExistsException.class,
                () -> taskService.createTask(userId, createDTO));
    }

    @Test
    void updateTask_ShouldUpdateTask() {
        // Arrange
        TaskUpdateDTO updateDTO = new TaskUpdateDTO("Updated Task", "New Desc", true, LocalDateTime.now());
        Task existingTask = new Task();
        TaskDTO taskDTO = new TaskDTO();

        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);
        when(taskMapper.toDTO(existingTask)).thenReturn(taskDTO);

        // Act
        TaskDTO result = taskService.updateTask(taskId, userId, updateDTO);

        // Assert
        assertEquals(taskDTO, result);
        verify(taskMapper).updateEntity(updateDTO, existingTask);
        verify(taskRepository).save(existingTask);
    }

    @Test
    void updateTask_ShouldThrowException_WhenTaskNotFound() {
        // Arrange
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> taskService.updateTask(taskId, userId, updateDTO));
    }

    @Test
    void toggleTaskStatus_ShouldToggleStatus() {
        // Arrange
        Task task = new Task();
        task.setActive(true);
        TaskDTO taskDTO = new TaskDTO();

        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDTO(task)).thenReturn(taskDTO);

        // Act
        TaskDTO result = taskService.toggleTaskStatus(taskId, userId);

        // Assert
        assertFalse(task.isActive());
        assertEquals(taskDTO, result);
    }

    @Test
    void toggleTaskStatus_ShouldThrowException_WhenTaskNotFound() {
        // Arrange
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> taskService.toggleTaskStatus(taskId, userId));
    }

    @Test
    void deleteTask_ShouldDeleteTask() {
        // Arrange
        Task task = new Task();
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));

        // Act
        taskService.deleteTask(taskId, userId);

        // Assert
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_ShouldThrowException_WhenTaskNotFound() {
        // Arrange
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> taskService.deleteTask(taskId, userId));
    }

    // Edge case tests
    @Test
    void getUserTasks_ShouldReturnEmptyList_WhenNoTasksFound() {
        // Arrange
        when(taskRepository.findByUserId(userId, false)).thenReturn(List.of());

        // Act
        List<TaskDTO> result = taskService.getUserTasks(userId, false);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void updateTask_ShouldNotUpdateCreatedAt_WhenNotProvided() {
        // Arrange
        TaskUpdateDTO updateDTO = new TaskUpdateDTO("Title", null, true, null);
        Task existingTask = new Task();
        existingTask.setCreatedAt(LocalDateTime.now());
        LocalDateTime originalDate = existingTask.getCreatedAt();

        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);
        when(taskMapper.toDTO(existingTask)).thenReturn(new TaskDTO());

        // Act
        taskService.updateTask(taskId, userId, updateDTO);

        // Assert
        assertEquals(originalDate, existingTask.getCreatedAt());
    }
}