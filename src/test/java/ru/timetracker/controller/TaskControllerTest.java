package ru.timetracker.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.timetracker.dto.task.TaskCreateDTO;
import ru.timetracker.dto.task.TaskDTO;
import ru.timetracker.dto.task.TaskUpdateDTO;
import ru.timetracker.service.TaskService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для {@link TaskController}. Проверяют корректность обработки HTTP-запросов, взаимодействие с {@link TaskService} и возвращаемые
 * статусы ответов.
 * <p>Проверяемые сценарии:
 * <ul>
 *   <li>Успешное выполнение операций CRUD</li>
 *   <li>Корректность HTTP-статусов в ответах</li>
 *   <li>Правильность передачи параметров в сервисный слой</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    /**
     * Проверяет успешное получение списка задач пользователя. Ожидаемое поведение:
     * <ul>
     *   <li>HTTP-статус 200 (OK)</li>
     *   <li>Тело ответа содержит список задач</li>
     *   <li>Вызов taskService.getUserTasks() с правильными параметрами</li>
     * </ul>
     */
    @Test
    void getUserTasks_Success() {
        Long userId = 1L;
        List<TaskDTO> expectedTasks = List.of(new TaskDTO());
        when(taskService.getUserTasks(userId, false)).thenReturn(expectedTasks);

        ResponseEntity<List<TaskDTO>> response = taskController.getUserTasks(userId, false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedTasks, response.getBody());
        verify(taskService).getUserTasks(userId, false);
    }

    /**
     * Проверяет успешное получение конкретной задачи. Ожидаемое поведение:
     * <ul>
     *   <li>HTTP-статус 200 (OK)</li>
     *   <li>Тело ответа содержит данные задачи</li>
     *   <li>Вызов taskService.getTaskById() с правильными параметрами</li>
     * </ul>
     */
    @Test
    void getTask_Success() {
        Long userId = 1L;
        Long taskId = 1L;
        TaskDTO expectedTask = new TaskDTO();
        when(taskService.getTaskById(userId, taskId)).thenReturn(expectedTask);

        ResponseEntity<TaskDTO> response = taskController.getTask(userId, taskId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedTask, response.getBody());
        verify(taskService).getTaskById(userId, taskId);
    }

    /**
     * Проверяет успешное создание новой задачи. Ожидаемое поведение:
     * <ul>
     *   <li>HTTP-статус 201 (Created)</li>
     *   <li>Тело ответа содержит созданную задачу</li>
     *   <li>Вызов taskService.createTask() с правильными параметрами</li>
     * </ul>
     */
    @Test
    void createTask_Success() {
        Long userId = 1L;
        TaskCreateDTO createDTO = new TaskCreateDTO();
        TaskDTO createdTask = new TaskDTO();
        when(taskService.createTask(userId, createDTO)).thenReturn(createdTask);

        ResponseEntity<TaskDTO> response = taskController.createTask(userId, createDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdTask, response.getBody());
        verify(taskService).createTask(userId, createDTO);
    }

    /**
     * Проверяет успешное обновление существующей задачи. Ожидаемое поведение:
     * <ul>
     *   <li>HTTP-статус 200 (OK)</li>
     *   <li>Тело ответа содержит обновленные данные</li>
     *   <li>Вызов taskService.updateTask() с правильными параметрами</li>
     * </ul>
     */
    @Test
    void updateTask_Success() {
        Long userId = 1L;
        Long taskId = 1L;
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        TaskDTO updatedTask = new TaskDTO();
        when(taskService.updateTask(taskId, userId, updateDTO)).thenReturn(updatedTask);

        ResponseEntity<TaskDTO> response = taskController.updateTask(userId, taskId, updateDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedTask, response.getBody());
        verify(taskService).updateTask(taskId, userId, updateDTO);
    }

    /**
     * Проверяет успешное изменение статуса задачи. Ожидаемое поведение:
     * <ul>
     *   <li>HTTP-статус 200 (OK)</li>
     *   <li>Тело ответа содержит задачу с обновленным статусом</li>
     *   <li>Вызов taskService.toggleTaskStatus() с правильными параметрами</li>
     * </ul>
     */
    @Test
    void toggleTaskStatus_Success() {
        Long userId = 1L;
        Long taskId = 1L;
        TaskDTO toggledTask = new TaskDTO();
        when(taskService.toggleTaskStatus(taskId, userId)).thenReturn(toggledTask);

        ResponseEntity<TaskDTO> response = taskController.toggleTaskStatus(userId, taskId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(toggledTask, response.getBody());
        verify(taskService).toggleTaskStatus(taskId, userId);
    }

    /**
     * Проверяет успешное удаление задачи. Ожидаемое поведение:
     * <ul>
     *   <li>HTTP-статус 204 (No Content)</li>
     *   <li>Вызов taskService.deleteTask() с правильными параметрами</li>
     * </ul>
     */
    @Test
    void deleteTask_Success() {
        Long userId = 1L;
        Long taskId = 1L;

        ResponseEntity<Void> response = taskController.deleteTask(userId, taskId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(taskService).deleteTask(taskId, userId);
    }
}