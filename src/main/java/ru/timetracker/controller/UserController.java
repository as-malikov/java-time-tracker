package ru.timetracker.controller;

import jakarta.validation.Valid;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.timetracker.dto.UserCreateDTO;
import ru.timetracker.dto.UserDTO;
import ru.timetracker.dto.UserUpdateDTO;
import ru.timetracker.service.UserService;

import java.util.List;

@Data
@RestController
@RequestMapping(path = "api/v1/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

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

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
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

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid UserCreateDTO userCreateDTO) {
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

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody @Valid UserUpdateDTO userUpdateDTO) {
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

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserCompletely(@PathVariable Long userId) {
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