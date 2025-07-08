package ru.timetracker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.timetracker.dto.mapper.UserMapper;
import ru.timetracker.dto.user.UserCreateDTO;
import ru.timetracker.dto.user.UserDTO;
import ru.timetracker.dto.user.UserUpdateDTO;
import ru.timetracker.exception.EmailAlreadyExistsException;
import ru.timetracker.exception.ResourceNotFoundException;
import ru.timetracker.model.User;
import ru.timetracker.repository.TaskRepository;
import ru.timetracker.repository.TimeEntryRepository;
import ru.timetracker.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @InjectMocks
    private UserService userService;

    private final Long userId = 1L;
    private final String email = "test@example.com";
    private final String newEmail = "new@example.com";

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        // Arrange
        User user = new User();
        UserDTO userDTO = new UserDTO();
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertEquals(1, result.size());
        assertEquals(userDTO, result.get(0));
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsersExist() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of());

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        // Arrange
        User user = new User();
        UserDTO userDTO = new UserDTO();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.getUserById(userId);

        // Assert
        assertEquals(userDTO, result);
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void createUser_ShouldCreateNewUser() {
        // Arrange
        UserCreateDTO createDTO = new UserCreateDTO("Test User", email);
        User user = new User();
        User savedUser = new User();
        UserDTO userDTO = new UserDTO();

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userMapper.toEntity(createDTO)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toDTO(savedUser)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.createUser(createDTO);

        // Assert
        assertEquals(userDTO, result);
        verify(userRepository).save(user);
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailExists() {
        // Arrange
        UserCreateDTO createDTO = new UserCreateDTO("Test User", email);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(createDTO));
    }

    @Test
    void updateUser_ShouldUpdateUser() {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO("Updated Name", newEmail);
        User existingUser = new User();
        existingUser.setEmail(email);
        UserDTO userDTO = new UserDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toDTO(existingUser)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.updateUser(userId, updateDTO);

        // Assert
        assertEquals(userDTO, result);
        assertEquals(newEmail, existingUser.getEmail());
        verify(userMapper).updateEntity(updateDTO, existingUser);
    }

    @Test
    void updateUser_ShouldNotCheckEmail_WhenNotChanged() {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO("Updated Name", email);
        User existingUser = new User();
        existingUser.setEmail(email);
        UserDTO userDTO = new UserDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toDTO(existingUser)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.updateUser(userId, updateDTO);

        // Assert
        assertEquals(userDTO, result);
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void updateUser_ShouldThrowException_WhenNewEmailExists() {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO("Updated Name", newEmail);
        User existingUser = new User();
        existingUser.setEmail(email);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(newEmail)).thenReturn(true);

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.updateUser(userId, updateDTO));
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(userId, updateDTO));
    }

    @Test
    void deleteUserCompletely_ShouldDeleteUserAndRelatedData() {
        // Arrange
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        userService.deleteUserCompletely(userId);

        // Assert
        verify(timeEntryRepository).deleteByUser(user);
        verify(taskRepository).deleteByUser(user);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserCompletely_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUserCompletely(userId));
    }

    // Edge case tests
    @Test
    void updateUser_ShouldUpdateOnlyName_WhenEmailNotProvided() {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO("New Name", null);
        User existingUser = new User();
        existingUser.setName("New Name");
        existingUser.setEmail(email);
        UserDTO userDTO = new UserDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toDTO(existingUser)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.updateUser(userId, updateDTO);

        // Assert
        assertEquals(userDTO, result);
        assertEquals("New Name", existingUser.getName());
        assertEquals(email, existingUser.getEmail());
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void createUser_ShouldHandleEmptyName() {
        // Arrange
        UserCreateDTO createDTO = new UserCreateDTO("Name", email);
        User user = new User();
        User savedUser = new User();
        UserDTO userDTO = new UserDTO();

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userMapper.toEntity(createDTO)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toDTO(savedUser)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.createUser(createDTO);

        // Assert
        assertEquals(userDTO, result);
    }
}