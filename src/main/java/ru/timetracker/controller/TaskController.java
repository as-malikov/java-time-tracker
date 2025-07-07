package ru.timetracker.controller;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.timetracker.dto.TaskCreateDTO;
import ru.timetracker.dto.TaskDTO;
import ru.timetracker.dto.TaskUpdateDTO;
import ru.timetracker.service.TaskService;

import java.util.List;

@Slf4j
@Data
@RestController
@RequestMapping(path = "/api/v1/users/{userId}/tasks")
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getUserTasks(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {

        log.info("Request to get tasks for user {} (includeInactive: {})", userId, includeInactive);

        try {
            List<TaskDTO> tasks = taskService.getUserTasks(userId, includeInactive);
            log.debug("Retrieved {} tasks for user {}", tasks.size(), userId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("Failed to get tasks for user {}. Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDTO> getTask(
            @PathVariable Long userId,
            @PathVariable Long taskId) {

        log.info("Request to get task {} for user {}", taskId, userId);

        try {
            TaskDTO task = taskService.getTaskById(userId, taskId);
            log.debug("Retrieved task {}: {}", taskId, task);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            log.error("Failed to get task {} for user {}. Error: {}", taskId, userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(
            @PathVariable Long userId,
            @RequestBody @Valid TaskCreateDTO taskCreateDTO) {

        log.info("Request to create task for user {}. Task data: {}", userId, taskCreateDTO);

        try {
            TaskDTO createdTask = taskService.createTask(userId, taskCreateDTO);
            log.info("Task created successfully. ID: {}, Name: '{}'",
                    createdTask.getId(), createdTask.getTitle());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        } catch (Exception e) {
            log.error("Failed to create task for user {}. Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long userId,
            @PathVariable Long taskId,
            @RequestBody @Valid TaskUpdateDTO taskUpdateDTO) {

        log.info("Request to update task {} for user {}. Update data: {}",
                taskId, userId, taskUpdateDTO);

        try {
            TaskDTO updatedTask = taskService.updateTask(taskId, userId, taskUpdateDTO);
            log.info("Task {} updated successfully. New status: {}",
                    taskId, updatedTask.isActive());
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            log.error("Failed to update task {} for user {}. Error: {}",
                    taskId, userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{taskId}/toggle-status")
    public ResponseEntity<TaskDTO> toggleTaskStatus(
            @PathVariable Long userId,
            @PathVariable Long taskId) {

        log.info("Request to toggle status for task {} (user {})", taskId, userId);

        try {
            TaskDTO toggledTask = taskService.toggleTaskStatus(taskId, userId);
            log.info("Task {} status toggled successfully. New status: {}",
                    taskId, toggledTask.isActive());
            return ResponseEntity.ok(toggledTask);
        } catch (Exception e) {
            log.error("Failed to toggle status for task {} (user {}). Error: {}",
                    taskId, userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long userId,
            @PathVariable Long taskId) {

        log.warn("Request to delete task {} for user {}", taskId, userId);

        try {
            taskService.deleteTask(taskId, userId);
            log.warn("Task {} deleted successfully by user {}", taskId, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete task {} for user {}. Error: {}",
                    taskId, userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}