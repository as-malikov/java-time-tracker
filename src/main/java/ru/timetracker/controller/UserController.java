package ru.timetracker.controller;

import jakarta.validation.Valid;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.timetracker.dto.user.UserCreateDTO;
import ru.timetracker.dto.user.UserDTO;
import ru.timetracker.dto.user.UserUpdateDTO;
import ru.timetracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * Контроллер для управления пользователями. Предоставляет REST API для операций CRUD (создание, чтение, обновление, удаление) с
 * пользователями. Базовый путь: /api/v1/users
 * <p>Контроллер включает следующие операции:
 * <ul>
 *   <li>Получение списка всех пользователей</li>
 *   <li>Получение пользователя по ID</li>
 *   <li>Создание нового пользователя</li>
 *   <li>Обновление данных пользователя</li>
 *   <li>Удаление пользователя</li>
 * </ul>
 * <p>Все методы возвращают стандартные HTTP-статусы и логируют выполнение операций.
 * @see UserService Сервис, реализующий бизнес-логику работы с пользователями
 * @see UserDTO DTO для передачи данных о пользователе
 */

@Data
@RestController
@RequestMapping(path = "api/v1/users")
@Tag(name = "User Management", description = "API for managing users")
public class UserController {
    private static final Logger logger = LogManager.getLogger(UserController.class);
    private final UserService userService;

    /**
     * Конструктор контроллера пользователей.
     * @param userService сервис для работы с пользователями
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Получает список всех зарегистрированных пользователей.
     * @return ResponseEntity со списком пользователей в формате JSON и статусом 200 OK, или статусом 500 в случае внутренней ошибки сервера
     */
    @Operation(summary = "Get all users", description = "Retrieves a list of all registered users")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        logger.info("Request to get all users");

        try {
            List<UserDTO> users = userService.getAllUsers();
            logger.debug("Successfully retrieved {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Failed to get users. Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Получает пользователя по его уникальному идентификатору.
     * @param id ID пользователя (обязательный)
     * @return ResponseEntity с данными пользователя и статусом 200 OK, или статусом 500 в случае ошибки
     */
    @Operation(summary = "Get user by ID", description = "Retrieves a single user by their unique identifier")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "User found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")})
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(
            @Parameter(description = "ID of the user to be retrieved", required = true) @PathVariable Long id) {
        logger.info("Request to get user by ID: {}", id);
        try {
            UserDTO user = userService.getUserById(id);
            logger.debug("User found: ID={}, Username={}", user.getId(), user.getName());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error getting user with ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .build();
        }
    }

    /**
     * Создает нового пользователя в системе.
     * @param userCreateDTO DTO с данными для создания пользователя (обязательный, валидируется)
     * @return ResponseEntity с созданным пользователем и статусом 201 Created, или статусом 400 при невалидных данных, 500 при внутренней
     * ошибке
     */
    @Operation(summary = "Create a new user", description = "Registers a new user in the system")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "User created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")})
    @PostMapping
    public ResponseEntity<UserDTO> createUser(
            @Parameter(description = "User data for registration", required = true) @RequestBody @Valid UserCreateDTO userCreateDTO) {
        logger.info("Request to create new user. Username: {}", userCreateDTO.getName());
        try {
            UserDTO createdUser = userService.createUser(userCreateDTO);
            logger.info("User created successfully. ID: {}, Username: {}", createdUser.getId(), createdUser.getName());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createdUser);
        } catch (Exception e) {
            logger.error("Error creating user. Username: {}", userCreateDTO.getName(), e);
            return ResponseEntity.internalServerError()
                    .build();
        }
    }

    /**
     * Обновляет данные существующего пользователя.
     * @param id            ID пользователя для обновления (обязательный)
     * @param userUpdateDTO DTO с обновленными данными пользователя (обязательный, валидируется)
     * @return ResponseEntity с обновленными данными пользователя и статусом 200 OK, или статусом 400/404/500 в случае ошибок
     */
    @Operation(summary = "Update user", description = "Updates an existing user's information")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "User updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")})
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "ID of the user to be updated", required = true) @PathVariable Long id,
            @Parameter(description = "Updated user data", required = true) @RequestBody @Valid UserUpdateDTO userUpdateDTO) {
        logger.info("Request to update user ID: {}", id);
        try {
            UserDTO updatedUser = userService.updateUser(id, userUpdateDTO);
            logger.info("User updated successfully. ID: {}, New username: {}", id, updatedUser.getName());
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error updating user ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .build();
        }
    }

    /**
     * Полностью удаляет пользователя из системы.
     * @param userId ID пользователя для удаления (обязательный)
     * @return ResponseEntity со статусом 204 No Content при успешном удалении, или статусом 404/500 в случае ошибок
     */
    @Operation(summary = "Delete user", description = "Permanently deletes a user from the system")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")})
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserCompletely(
            @Parameter(description = "ID of the user to be deleted", required = true) @PathVariable Long userId) {
        logger.info("Request to delete user ID: {}", userId);
        try {
            userService.deleteUserCompletely(userId);
            logger.info("User deleted successfully. ID: {}", userId);
            return ResponseEntity.noContent()
                    .build();
        } catch (Exception e) {
            logger.error("Error deleting user ID: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .build();
        }
    }
}