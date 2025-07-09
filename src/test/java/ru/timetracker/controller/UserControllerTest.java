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

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserService userService;

    @InjectMocks private UserController userController;

    @Test
    void getAllUsers_Success() {
        List<UserDTO> users = List.of(new UserDTO());
        when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<UserDTO>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
        verify(userService).getAllUsers();
    }

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

    @Test
    void deleteUserCompletely_Success() {
        Long userId = 1L;

        ResponseEntity<Void> response = userController.deleteUserCompletely(userId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).deleteUserCompletely(userId);
    }
}