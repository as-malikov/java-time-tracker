package ru.timetracker.controller;

import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.HttpStatus;
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
    private final TaskService taskService;

    @GetMapping
    public List<TaskDTO> getUserTasks(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return taskService.getUserTasks(userId, includeInactive);
    }

    @GetMapping("/{taskId}")
    public TaskDTO getTask(
            @PathVariable Long userId,
            @PathVariable Long taskId) {
        return taskService.getTaskById(userId, taskId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO createTask(
            @PathVariable Long userId,
            @RequestBody @Valid TaskCreateDTO taskCreateDTO) {
        return taskService.createTask(userId, taskCreateDTO);
    }

    @PutMapping("/{taskId}")
    public TaskDTO updateTask(
            @PathVariable Long userId,
            @PathVariable Long taskId,
            @RequestBody @Valid TaskUpdateDTO taskUpdateDTO) {
        return taskService.updateTask(taskId, userId, taskUpdateDTO);
    }

    @PatchMapping("/{taskId}/toggle-status")
    public TaskDTO toggleTaskStatus(
            @PathVariable Long userId,
            @PathVariable Long taskId) {
        return taskService.toggleTaskStatus(taskId, userId);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(
            @PathVariable Long userId,
            @PathVariable Long taskId) {
        taskService.deleteTask(taskId, userId);
    }
}
