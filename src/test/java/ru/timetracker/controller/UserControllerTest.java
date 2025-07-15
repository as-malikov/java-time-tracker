package ru.timetracker.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.timetracker.dto.user.UserCreateDTO;
import ru.timetracker.dto.user.UserDTO;
import ru.timetracker.dto.user.UserUpdateDTO;
import ru.timetracker.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для {@link UserController}. Проверяют корректность работы с пользователями, включая CRUD-операции и валидацию данных.
 * <p>Основные проверяемые сценарии:
 * <ul>
 *   <li>Получение списка пользователей и данных конкретного пользователя</li>
 *   <li>Создание, обновление и удаление пользователей</li>
 *   <li>Корректность HTTP-статусов в ответах</li>
 *   <li>Правильность передачи параметров в сервисный слой</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    /**
     * Проверяет успешное получение списка всех пользователей. Ожидаемое поведение:
     * <ul>
     *   <li>HTTP-статус 200 (OK)</li>
     *   <li>Тело ответа содержит список пользователей</li>
     *   <li>Вызов userService.getAllUsers() без параметров</li>
     * </ul>
     */
    @Test
    void getAllUsers_Success() {
        List<UserDTO> users = List.of(new UserDTO());
        when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<UserDTO>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
        verify(userService).getAllUsers();
    }

    /**
     * Проверяет успешное получение данных пользователя по ID. Ожидаемое поведение:
     * <ul>
     *   <li>HTTP-статус 200 (OK)</li>
     *   <li>Тело ответа содержит данные пользователя</li>
     *   <li>Вызов userService.getUserById() с правильным ID</li>
     * </ul>
     */
    @Test
    void getUser_Success() {
        Long id = 1L;
        UserDTO user = new UserDTO();
        when(userService.getUserById(id)).thenReturn(user);

        ResponseEntity<UserDTO> response = userController.getUser(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
        verify(userService).getUserById(id);
    }

    /**
     * Проверяет успешное создание нового пользователя. Ожидаемое поведение:
     * <ul>
     *   <li>HTTP-статус 201 (Created)</li>
     *   <li>Тело ответа содержит данные созданного пользователя</li>
     *   <li>Вызов userService.createUser() с переданными данными</li>
     * </ul>
     */
    @Test
    void createUser_Success() {
        UserCreateDTO createDTO = new UserCreateDTO();
        UserDTO createdUser = new UserDTO();
        when(userService.createUser(createDTO)).thenReturn(createdUser);

        ResponseEntity<UserDTO> response = userController.createUser(createDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdUser, response.getBody());
        verify(userService).createUser(createDTO);
    }

    /**
     * Проверяет успешное обновление данных пользователя. Ожидаемое поведение:
     * <ul>
     *   <li>HTTP-статус 200 (OK)</li>
     *   <li>Тело ответа содержит обновленные данные</li>
     *   <li>Вызов userService.updateUser() с правильными ID и данными</li>
     * </ul>
     */
    @Test
    void updateUser_Success() {
        Long id = 1L;
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        UserDTO updatedUser = new UserDTO();
        when(userService.updateUser(id, updateDTO)).thenReturn(updatedUser);

        ResponseEntity<UserDTO> response = userController.updateUser(id, updateDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedUser, response.getBody());
        verify(userService).updateUser(id, updateDTO);
    }

    /**
     * Проверяет успешное полное удаление пользователя. Ожидаемое поведение:
     * <ul>
     *   <li>HTTP-статус 204 (No Content)</li>
     *   <li>Вызов userService.deleteUserCompletely() с правильным ID</li>
     * </ul>
     */
    @Test
    void deleteUserCompletely_Success() {
        Long userId = 1L;

        ResponseEntity<Void> response = userController.deleteUserCompletely(userId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).deleteUserCompletely(userId);
    }
}