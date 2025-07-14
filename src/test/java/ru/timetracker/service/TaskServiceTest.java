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

/**
 * Тесты для сервиса {@link TaskService}, проверяющие корректность работы с задачами пользователей.
 * <p>Включает проверки:
 * <ul>
 *   <li>Создания задач с валидацией уникальности названия</li>
 *   <li>Чтения задач (поиск по ID, получение списка с фильтрацией по статусу)</li>
 *   <li>Обновления данных задачи (включая частичное обновление полей)</li>
 *   <li>Удаления задач и связанных данных</li>
 *   <li>Изменения статуса активности задачи</li>
 *   <li>Обработки ошибочных сценариев (несуществующие задачи/пользователи)</li>
 * </ul>
 * <p>Использует моки репозиториев для изолированного тестирования бизнес-логики.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private final Long userId = 1L;
    private final Long taskId = 1L;
    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskMapper taskMapper;
    @InjectMocks private TaskService taskService;

    /**
     * Проверяет получение отфильтрованного списка задач пользователя.
     * <p>Ожидается:
     * <ul>
     *   <li>Вызов taskRepository.findByUserId с правильными параметрами</li>
     *   <li>Преобразование сущностей в DTO</li>
     *   <li>Возврат непустого списка при наличии задач</li>
     * </ul>
     */
    @Test
    void getUserTasks_ShouldReturnTasks() {
        Task task = new Task();
        TaskDTO taskDTO = new TaskDTO();
        when(taskRepository.findByUserId(userId, true)).thenReturn(List.of(task));
        when(taskMapper.toDTO(task)).thenReturn(taskDTO);

        List<TaskDTO> result = taskService.getUserTasks(userId, true);

        assertEquals(1, result.size());
        assertEquals(taskDTO, result.get(0));
        verify(taskRepository).findByUserId(userId, true);
    }

    /**
     * Проверяет получение конкретной задачи по ID пользователя и ID задачи.
     * <p>Ожидается:
     * <ul>
     *   <li>Поиск в репозитории по двум идентификаторам</li>
     *   <li>Преобразование найденной сущности в DTO</li>
     *   <li>Возврат DTO при успешном нахождении задачи</li>
     * </ul>
     */
    @Test
    void getTaskById_ShouldReturnTask_WhenExists() {
        Task task = new Task();
        TaskDTO taskDTO = new TaskDTO();
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));
        when(taskMapper.toDTO(task)).thenReturn(taskDTO);

        TaskDTO result = taskService.getTaskById(userId, taskId);

        assertEquals(taskDTO, result);
        verify(taskRepository).findByIdAndUserId(taskId, userId);
    }

    /**
     * Проверяет обработку случая отсутствия задачи при запросе.
     * <p>Ожидается:
     * <ul>
     *   <li>Выброс ResourceNotFoundException</li>
     *   <li>Сообщение об ошибке содержит информацию о ненайденной задаче</li>
     * </ul>
     */
    @Test
    void getTaskById_ShouldThrowException_WhenNotFound() {
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(userId, taskId));
    }

    /**
     * Проверяет создание новой задачи с валидными параметрами.
     * <p>Ожидается:
     * <ul>
     *   <li>Проверка уникальности названия задачи для пользователя</li>
     *   <li>Корректное преобразование DTO в сущность</li>
     *   <li>Сохранение задачи с привязкой к пользователю</li>
     *   <li>Возврат DTO созданной задачи</li>
     * </ul>
     */
    @Test
    void createTask_ShouldCreateNewTask() {
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

        TaskDTO result = taskService.createTask(userId, createDTO);

        assertEquals(taskDTO, result);
        verify(taskRepository).save(task);
        assertEquals(user, task.getUser());
    }

    /**
     * Проверяет обработку случая отсутствия пользователя при создании задачи.
     * <p>Ожидается:
     * <ul>
     *   <li>Выброс ResourceNotFoundException</li>
     *   <li>Отмена операции создания задачи</li>
     * </ul>
     */
    @Test
    void createTask_ShouldThrowException_WhenUserNotFound() {
        TaskCreateDTO createDTO = new TaskCreateDTO("New Task", "Description");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(userId, createDTO));
    }

    /**
     * Проверяет обработку конфликта названий задач для одного пользователя.
     * <p>Ожидается:
     * <ul>
     *   <li>Выброс TaskAlreadyExistsException</li>
     *   <li>Отмена операции сохранения</li>
     *   <li>Сообщение об ошибке содержит информацию о дублировании</li>
     * </ul>
     */
    @Test
    void createTask_ShouldThrowException_WhenTitleExists() {
        TaskCreateDTO createDTO = new TaskCreateDTO("Existing Task", "Description");
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.existsByUserIdAndTitle(userId, createDTO.getTitle())).thenReturn(true);

        assertThrows(TaskAlreadyExistsException.class, () -> taskService.createTask(userId, createDTO));
    }

    /**
     * Проверяет полное обновление данных задачи.
     * <p>Ожидается:
     * <ul>
     *   <li>Обновление всех переданных полей (название, описание, статус, дата)</li>
     *   <li>Сохранение обновленной сущности</li>
     *   <li>Возврат актуального DTO</li>
     * </ul>
     */
    @Test
    void updateTask_ShouldUpdateTask() {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO("Updated Task", "New Desc", true, LocalDateTime.now());
        Task existingTask = new Task();
        TaskDTO taskDTO = new TaskDTO();

        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);
        when(taskMapper.toDTO(existingTask)).thenReturn(taskDTO);

        TaskDTO result = taskService.updateTask(taskId, userId, updateDTO);

        assertEquals(taskDTO, result);
        verify(taskMapper).updateEntity(updateDTO, existingTask);
        verify(taskRepository).save(existingTask);
    }

    /**
     * Проверяет обработку случая отсутствия задачи при обновлении.
     * <p>Ожидается:
     * <ul>
     *   <li>Выброс ResourceNotFoundException</li>
     *   <li>Отмена операции обновления</li>
     * </ul>
     */
    @Test
    void updateTask_ShouldThrowException_WhenTaskNotFound() {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(taskId, userId, updateDTO));
    }

    /**
     * Проверяет изменение статуса активности задачи.
     * <p>Ожидается:
     * <ul>
     *   <li>Инверсия текущего статуса активности (true → false или false → true)</li>
     *   <li>Сохранение обновленной задачи</li>
     *   <li>Возврат актуального DTO</li>
     * </ul>
     */
    @Test
    void toggleTaskStatus_ShouldToggleStatus() {
        Task task = new Task();
        task.setActive(true);
        TaskDTO taskDTO = new TaskDTO();

        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDTO(task)).thenReturn(taskDTO);

        TaskDTO result = taskService.toggleTaskStatus(taskId, userId);

        assertFalse(task.isActive());
        assertEquals(taskDTO, result);
    }

    /**
     * Проверяет обработку случая отсутствия задачи при изменении статуса.
     * <p>Ожидается:
     * <ul>
     *   <li>Выброс ResourceNotFoundException</li>
     *   <li>Отмена операции изменения статуса</li>
     * </ul>
     */
    @Test
    void toggleTaskStatus_ShouldThrowException_WhenTaskNotFound() {
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.toggleTaskStatus(taskId, userId));
    }

    /**
     * Проверяет удаление задачи.
     * <p>Ожидается:
     * <ul>
     *   <li>Поиск задачи по ID пользователя и ID задачи</li>
     *   <li>Удаление найденной задачи</li>
     *   <li>Отсутствие исключений при успешном удалении</li>
     * </ul>
     */
    @Test
    void deleteTask_ShouldDeleteTask() {
        Task task = new Task();
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));

        taskService.deleteTask(taskId, userId);

        verify(taskRepository).delete(task);
    }

    /**
     * Проверяет обработку случая отсутствия задачи при удалении.
     * <p>Ожидается:
     * <ul>
     *   <li>Выброс ResourceNotFoundException</li>
     *   <li>Отмена операции удаления</li>
     * </ul>
     */
    @Test
    void deleteTask_ShouldThrowException_WhenTaskNotFound() {
        // Arrange
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(taskId, userId));
    }

    /**
     * Проверяет получение пустого списка задач, когда у пользователя нет задач.
     * <p>Ожидается:
     * <ul>
     *   <li>Возврат пустого списка</li>
     *   <li>Отсутствие исключений</li>
     *   <li>Корректный вызов репозитория</li>
     * </ul>
     */
    @Test
    void getUserTasks_ShouldReturnEmptyList_WhenNoTasksFound() {
        when(taskRepository.findByUserId(userId, false)).thenReturn(List.of());

        List<TaskDTO> result = taskService.getUserTasks(userId, false);

        assertTrue(result.isEmpty());
    }

    /**
     * Проверяет сохранение даты создания при обновлении задачи.
     * <p>Ожидается:
     * <ul>
     *   <li>Дата создания остается неизменной</li>
     *   <li>Обновляются только указанные поля</li>
     *   <li>Отсутствие нежелательных изменений системных полей</li>
     * </ul>
     */
    @Test
    void updateTask_ShouldNotUpdateCreatedAt_WhenNotProvided() {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO("Title", null, true, null);
        Task existingTask = new Task();
        existingTask.setCreatedAt(LocalDateTime.now());
        LocalDateTime originalDate = existingTask.getCreatedAt();

        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);
        when(taskMapper.toDTO(existingTask)).thenReturn(new TaskDTO());

        taskService.updateTask(taskId, userId, updateDTO);

        assertEquals(originalDate, existingTask.getCreatedAt());
    }
}