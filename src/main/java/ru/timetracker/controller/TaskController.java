package ru.timetracker.controller;

import jakarta.validation.Valid;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.timetracker.dto.TaskCreateDTO;
import ru.timetracker.dto.TaskDTO;
import ru.timetracker.dto.TaskUpdateDTO;
import ru.timetracker.service.TaskService;

import java.util.List;

@Data
@RestController
@RequestMapping(path = "/api/v1/users/{userId}/tasks")
public class TaskController {
    private static final Logger logger = LogManager.getLogger(TaskController.class);
    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getUserTasks(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {

        logger.info("Получение задач для пользователя {} (includeInactive: {})", userId, includeInactive);

        try {
            List<TaskDTO> tasks = taskService.getUserTasks(userId, includeInactive);
            logger.debug("Успешно получено {} задач для пользователя {}", tasks.size(), userId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Ошибка при получении задач для пользователя {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDTO> getTask(
            @PathVariable Long userId,
            @PathVariable Long taskId) {

        logger.info("Получение задачи {} для пользователя {}", taskId, userId);

        try {
            TaskDTO task = taskService.getTaskById(userId, taskId);
            logger.debug("Успешно получена задача {}: {}", taskId, task);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            logger.error("Ошибка при получении задачи {} для пользователя {}: {}", taskId, userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(
            @PathVariable Long userId,
            @RequestBody @Valid TaskCreateDTO taskCreateDTO) {

        logger.info("Создание задачи для пользователя {}. Данные: {}", userId, taskCreateDTO);

        try {
            TaskDTO createdTask = taskService.createTask(userId, taskCreateDTO);
            logger.info("Задача успешно создана. ID: {}, Название: {}", createdTask.getId(), createdTask.getTitle());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        } catch (Exception e) {
            logger.error("Ошибка при создании задачи для пользователя {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long userId,
            @PathVariable Long taskId,
            @RequestBody @Valid TaskUpdateDTO taskUpdateDTO) {

        logger.info("Обновление задачи {} для пользователя {}. Данные: {}", taskId, userId, taskUpdateDTO);

        try {
            TaskDTO updatedTask = taskService.updateTask(taskId, userId, taskUpdateDTO);
            logger.info("Задача {} успешно обновлена. Новый статус: {}", taskId, updatedTask.isActive());
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            logger.error("Ошибка при обновлении задачи {} для пользователя {}: {}", taskId, userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{taskId}/toggle-status")
    public ResponseEntity<TaskDTO> toggleTaskStatus(
            @PathVariable Long userId,
            @PathVariable Long taskId) {

        logger.info("Переключение статуса задачи {} для пользователя {}", taskId, userId);

        try {
            TaskDTO toggledTask = taskService.toggleTaskStatus(taskId, userId);
            logger.info("Статус задачи {} успешно изменен. Новый статус: {}", taskId, toggledTask.isActive());
            return ResponseEntity.ok(toggledTask);
        } catch (Exception e) {
            logger.error("Ошибка при переключении статуса задачи {} для пользователя {}: {}", taskId, userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long userId,
            @PathVariable Long taskId) {

        logger.warn("Удаление задачи {} для пользователя {}", taskId, userId);

        try {
            taskService.deleteTask(taskId, userId);
            logger.warn("Задача {} успешно удалена пользователем {}", taskId, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Ошибка при удалении задачи {} для пользователя {}: {}", taskId, userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}