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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Data
@RestController
@RequestMapping(path = "/api/v1/users/{userId}/tasks")
@Tag(name = "Task Management", description = "API for managing user tasks")
public class TaskController {
    private static final Logger logger = LogManager.getLogger(TaskController.class);
    private final TaskService taskService;

    @Operation(
            summary = "Get user tasks",
            description = "Retrieves all tasks for a specific user, with option to include inactive tasks"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskDTO.class, type = "array"))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<TaskDTO>> getUserTasks(
            @Parameter(description = "ID of the user whose tasks to retrieve", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Whether to include inactive tasks", example = "false")
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

    @Operation(
            summary = "Get task by ID",
            description = "Retrieves a specific task for a user by task ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskDTO.class))),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDTO> getTask(
            @Parameter(description = "ID of the user who owns the task", required = true)
            @PathVariable Long userId,
            @Parameter(description = "ID of the task to retrieve", required = true)
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

    @Operation(
            summary = "Create new task",
            description = "Creates a new task for the specified user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(
            @Parameter(description = "ID of the user who will own the task", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Task data to create", required = true)
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

    @Operation(
            summary = "Update task",
            description = "Updates an existing task for a user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Task or user not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(
            @Parameter(description = "ID of the user who owns the task", required = true)
            @PathVariable Long userId,
            @Parameter(description = "ID of the task to update", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "Updated task data", required = true)
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

    @Operation(
            summary = "Toggle task status",
            description = "Toggles the active/inactive status of a task"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task status toggled successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskDTO.class))),
            @ApiResponse(responseCode = "404", description = "Task or user not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{taskId}/toggle-status")
    public ResponseEntity<TaskDTO> toggleTaskStatus(
            @Parameter(description = "ID of the user who owns the task", required = true)
            @PathVariable Long userId,
            @Parameter(description = "ID of the task to toggle status", required = true)
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

    @Operation(
            summary = "Delete task",
            description = "Permanently deletes a task"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task or user not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "ID of the user who owns the task", required = true)
            @PathVariable Long userId,
            @Parameter(description = "ID of the task to delete", required = true)
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