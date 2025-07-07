package ru.timetracker.service;

import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger logger = LogManager.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    @Transactional(readOnly = true)
    public List<TaskDTO> getUserTasks(Long userId, boolean includeInactive) {
        logger.debug("Получение задач для пользователя ID: {}, includeInactive: {}", userId, includeInactive);
        List<TaskDTO> tasks = taskRepository.findByUserId(userId, includeInactive).stream()
                .map(taskMapper::toDTO)
                .toList();
        logger.info("Найдено {} задач для пользователя ID: {}", tasks.size(), userId);
        return tasks;
    }

    @Transactional(readOnly = true)
    public TaskDTO getTaskById(Long userId, Long taskId) {
        logger.debug("Поиск задачи ID: {} для пользователя ID: {}", taskId, userId);
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> {
                    String errorMsg = "Задача не найдена. ID задачи: " + taskId + ", ID пользователя: " + userId;
                    logger.error(errorMsg);
                    return new ResourceNotFoundException(errorMsg);
                });
        logger.info("Найдена задача ID: {} для пользователя ID: {}", taskId, userId);
        return taskMapper.toDTO(task);
    }

    @Transactional
    public TaskDTO createTask(Long userId, TaskCreateDTO taskCreateDTO) {
        logger.debug("Создание новой задачи для пользователя ID: {}. Данные: {}", userId, taskCreateDTO);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    String errorMsg = "Пользователь не найден. ID: " + userId;
                    logger.error(errorMsg);
                    return new ResourceNotFoundException(errorMsg);
                });

        if (taskRepository.existsByUserIdAndTitle(userId, taskCreateDTO.getTitle())) {
            String errorMsg = "Задача с таким названием уже существует: " + taskCreateDTO.getTitle();
            logger.error(errorMsg);
            throw new TaskAlreadyExistsException(taskCreateDTO.getTitle());
        }

        Task task = taskMapper.toEntity(taskCreateDTO);
        task.setUser(user);
        task = taskRepository.save(task);

        logger.info("Создана новая задача ID: {} для пользователя ID: {}. Название: {}",
                task.getId(), userId, task.getTitle());
        return taskMapper.toDTO(task);
    }

    @Transactional
    public TaskDTO updateTask(Long taskId, Long userId, TaskUpdateDTO taskUpdateDTO) {
        logger.debug("Обновление задачи ID: {} для пользователя ID: {}. Данные: {}", taskId, userId, taskUpdateDTO);

        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> {
                    String errorMsg = "Задача не найдена. ID задачи: " + taskId + ", ID пользователя: " + userId;
                    logger.error(errorMsg);
                    return new ResourceNotFoundException(errorMsg);
                });

        taskMapper.updateEntity(taskUpdateDTO, task);

        if (taskUpdateDTO.getCreatedAt() != null) {
            task.setCreatedAt(taskUpdateDTO.getCreatedAt());
            logger.debug("Обновлено поле createdAt для задачи ID: {}", taskId);
        }

        task = taskRepository.save(task);
        logger.info("Обновлена задача ID: {} для пользователя ID: {}", taskId, userId);
        return taskMapper.toDTO(task);
    }

    @Transactional
    public TaskDTO toggleTaskStatus(Long taskId, Long userId) {
        logger.debug("Изменение статуса задачи ID: {} для пользователя ID: {}", taskId, userId);

        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> {
                    String errorMsg = "Задача не найдена. ID задачи: " + taskId + ", ID пользователя: " + userId;
                    logger.error(errorMsg);
                    return new ResourceNotFoundException(errorMsg);
                });

        boolean newStatus = !task.isActive();
        task.setActive(newStatus);
        task = taskRepository.save(task);

        logger.info("Изменен статус задачи ID: {} для пользователя ID: {}. Новый статус: {}",
                taskId, userId, newStatus ? "активна" : "неактивна");
        return taskMapper.toDTO(task);
    }

    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        logger.debug("Удаление задачи ID: {} для пользователя ID: {}", taskId, userId);

        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> {
                    String errorMsg = "Задача не найдена. ID задачи: " + taskId + ", ID пользователя: " + userId;
                    logger.error(errorMsg);
                    return new ResourceNotFoundException(errorMsg);
                });

        taskRepository.delete(task);
        logger.info("Удалена задача ID: {} для пользователя ID: {}", taskId, userId);
    }
}