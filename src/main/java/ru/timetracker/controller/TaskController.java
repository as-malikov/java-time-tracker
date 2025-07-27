package ru.timetracker.controller;

import jakarta.validation.Valid;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.timetracker.dto.task.TaskCreateDTO;
import ru.timetracker.dto.task.TaskDTO;
import ru.timetracker.dto.task.TaskUpdateDTO;
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

        logger.info("Fetching tasks for user {} (includeInactive: {})", userId, includeInactive);

        try {
            List<TaskDTO> tasks = taskService.getUserTasks(userId, includeInactive);
            logger.debug("Successfully retrieved {} tasks for user {}", tasks.size(), userId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Error fetching tasks for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDTO> getTask(
            @PathVariable Long userId,
            @PathVariable Long taskId) {

        logger.info("Fetching task {} for user {}", taskId, userId);

        try {
            TaskDTO task = taskService.getTaskById(userId, taskId);
            logger.debug("Successfully retrieved task {}: {}", taskId, task);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            logger.error("Error fetching task {} for user {}: {}", taskId, userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(
            @PathVariable Long userId,
            @RequestBody @Valid TaskCreateDTO taskCreateDTO) {

        logger.info("Creating task for user {}. Data: {}", userId, taskCreateDTO);

        try {
            TaskDTO createdTask = taskService.createTask(userId, taskCreateDTO);
            logger.info("Task created successfully. ID: {}, Title: {}", createdTask.getId(), createdTask.getTitle());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        } catch (Exception e) {
            logger.error("Error creating task for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long userId,
            @PathVariable Long taskId,
            @RequestBody @Valid TaskUpdateDTO taskUpdateDTO) {

        logger.info("Updating task {} for user {}. Data: {}", taskId, userId, taskUpdateDTO);

        try {
            TaskDTO updatedTask = taskService.updateTask(taskId, userId, taskUpdateDTO);
            logger.info("Task {} updated successfully. New status: {}", taskId, updatedTask.isActive());
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            logger.error("Error updating task {} for user {}: {}", taskId, userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{taskId}/toggle-status")
    public ResponseEntity<TaskDTO> toggleTaskStatus(
            @PathVariable Long userId,
            @PathVariable Long taskId) {

        logger.info("Toggling status for task {} for user {}", taskId, userId);

        try {
            TaskDTO toggledTask = taskService.toggleTaskStatus(taskId, userId);
            logger.info("Task {} status toggled successfully. New status: {}", taskId, toggledTask.isActive());
            return ResponseEntity.ok(toggledTask);
        } catch (Exception e) {
            logger.error("Error toggling status for task {} for user {}: {}", taskId, userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long userId,
            @PathVariable Long taskId) {

        logger.warn("Deleting task {} for user {}", taskId, userId);

        try {
            taskService.deleteTask(taskId, userId);
            logger.warn("Task {} deleted successfully by user {}", taskId, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting task {} for user {}: {}", taskId, userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}