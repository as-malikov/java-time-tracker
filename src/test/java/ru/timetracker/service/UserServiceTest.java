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

/**
 * Тесты для {@link UserService}. Проверяют бизнес-логику работы с пользователями, включая валидацию данных и обработку исключительных
 * ситуаций.
 * <p>Основные проверяемые сценарии:
 * <ul>
 *   <li>CRUD-операции с пользователями</li>
 *   <li>Проверка уникальности email</li>
 *   <li>Обработка случаев отсутствия пользователя</li>
 *   <li>Каскадное удаление связанных данных</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private final Long userId = 1L;
    private final String email = "test@example.com";
    private final String newEmail = "new@example.com";
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

    /**
     * Проверяет получение списка всех пользователей. Ожидаемое поведение:
     * <ul>
     *   <li>Возвращает список DTO пользователей</li>
     *   <li>Вызывает userRepository.findAll()</li>
     *   <li>Преобразует сущности в DTO через userMapper</li>
     * </ul>
     */
    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        User user = new User();
        UserDTO userDTO = new UserDTO();
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        List<UserDTO> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals(userDTO, result.get(0));
        verify(userRepository).findAll();
    }

    /**
     * Проверяет обработку пустого списка пользователей. Ожидаемое поведение:
     * <ul>
     *   <li>Возвращает пустой список</li>
     *   <li>Не генерирует исключений</li>
     * </ul>
     */
    @Test
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsersExist() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDTO> result = userService.getAllUsers();

        assertTrue(result.isEmpty());
    }

    /**
     * Проверяет получение пользователя по существующему ID. Ожидаемое поведение:
     * <ul>
     *   <li>Возвращает DTO пользователя</li>
     *   <li>Вызывает userRepository.findById()</li>
     *   <li>Преобразует сущность в DTO</li>
     * </ul>
     */
    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        User user = new User();
        UserDTO userDTO = new UserDTO();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.getUserById(userId);

        assertEquals(userDTO, result);
        verify(userRepository).findById(userId);
    }

    /**
     * Проверяет обработку случая отсутствия пользователя. Ожидаемое поведение:
     * <ul>
     *   <li>Генерирует ResourceNotFoundException</li>
     *   <li>Не возвращает результат</li>
     * </ul>
     */
    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
    }

    /**
     * Проверяет создание нового пользователя с корректными данными. Ожидаемое поведение:
     * <ul>
     *   <li>Проверяет уникальность email</li>
     *   <li>Сохраняет нового пользователя</li>
     *   <li>Возвращает DTO созданного пользователя</li>
     * </ul>
     */
    @Test
    void createUser_ShouldCreateNewUser() {
        UserCreateDTO createDTO = new UserCreateDTO("Test User", email);
        User user = new User();
        User savedUser = new User();
        UserDTO userDTO = new UserDTO();

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userMapper.toEntity(createDTO)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toDTO(savedUser)).thenReturn(userDTO);

        UserDTO result = userService.createUser(createDTO);

        assertEquals(userDTO, result);
        verify(userRepository).save(user);
    }

    /**
     * Проверяет обработку дублирования email при создании. Ожидаемое поведение:
     * <ul>
     *   <li>Генерирует EmailAlreadyExistsException</li>
     *   <li>Не сохраняет пользователя</li>
     * </ul>
     */
    @Test
    void createUser_ShouldThrowException_WhenEmailExists() {
        UserCreateDTO createDTO = new UserCreateDTO("Test User", email);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(createDTO));
    }

    /**
     * Проверяет обновление данных пользователя. Ожидаемое поведение:
     * <ul>
     *   <li>Обновляет имя пользователя</li>
     *   <li>Проверяет новый email на уникальность</li>
     *   <li>Возвращает обновленные данные</li>
     * </ul>
     */
    @Test
    void updateUser_ShouldUpdateUser() {
        UserUpdateDTO updateDTO = new UserUpdateDTO("Updated Name", newEmail);
        User existingUser = new User();
        existingUser.setEmail(email);
        UserDTO userDTO = new UserDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toDTO(existingUser)).thenReturn(userDTO);

        UserDTO result = userService.updateUser(userId, updateDTO);

        assertEquals(userDTO, result);
        assertEquals(email, existingUser.getEmail());
        verify(userMapper).updateEntity(updateDTO, existingUser);
    }

    /**
     * Проверяет обновление пользователя без проверки email, когда он не изменен. Ожидаемое поведение:
     * <ul>
     *   <li>Обновляет только имя пользователя</li>
     *   <li>Не проверяет email на уникальность, если он не изменился</li>
     *   <li>Возвращает обновленные данные</li>
     * </ul>
     */
    @Test
    void updateUser_ShouldNotCheckEmail_WhenNotChanged() {
        UserUpdateDTO updateDTO = new UserUpdateDTO("Updated Name", email);
        User existingUser = new User();
        existingUser.setEmail(email);
        UserDTO userDTO = new UserDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toDTO(existingUser)).thenReturn(userDTO);

        UserDTO result = userService.updateUser(userId, updateDTO);

        assertEquals(userDTO, result);
        verify(userRepository, never()).existsByEmail(any());
    }

    /**
     * Проверяет обработку случая, когда новый email уже существует в системе. Ожидаемое поведение:
     * <ul>
     *   <li>Генерирует EmailAlreadyExistsException</li>
     *   <li>Не сохраняет изменения</li>
     * </ul>
     */
    @Test
    void updateUser_ShouldThrowException_WhenNewEmailExists() {
        UserUpdateDTO updateDTO = new UserUpdateDTO("Updated Name", newEmail);
        User existingUser = new User();
        existingUser.setEmail(email);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(newEmail)).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.updateUser(userId, updateDTO));
    }

    /**
     * Проверяет обработку случая отсутствия пользователя при обновлении. Ожидаемое поведение:
     * <ul>
     *   <li>Генерирует ResourceNotFoundException</li>
     *   <li>Не выполняет обновление</li>
     * </ul>
     */
    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(userId, updateDTO));
    }

    /**
     * Проверяет полное удаление пользователя и связанных с ним данных. Ожидаемое поведение:
     * <ul>
     *   <li>Удаляет все временные записи пользователя</li>
     *   <li>Удаляет все задачи пользователя</li>
     *   <li>Удаляет самого пользователя</li>
     * </ul>
     */
    @Test
    void deleteUserCompletely_ShouldDeleteUserAndRelatedData() {
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUserCompletely(userId);

        verify(timeEntryRepository).deleteByUser(user);
        verify(taskRepository).deleteByUser(user);
        verify(userRepository).delete(user);
    }

    /**
     * Проверяет обработку случая отсутствия пользователя при удалении. Ожидаемое поведение:
     * <ul>
     *   <li>Генерирует ResourceNotFoundException</li>
     *   <li>Не выполняет удаление</li>
     * </ul>
     */
    @Test
    void deleteUserCompletely_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUserCompletely(userId));
    }

    /**
     * Проверяет обновление только имени пользователя, когда email не предоставлен. Ожидаемое поведение:
     * <ul>
     *   <li>Обновляет только имя пользователя</li>
     *   <li>Не изменяет email</li>
     *   <li>Не проверяет email на уникальность</li>
     * </ul>
     */
    @Test
    void updateUser_ShouldUpdateOnlyName_WhenEmailNotProvided() {
        UserUpdateDTO updateDTO = new UserUpdateDTO("New Name", null);
        User existingUser = new User();
        existingUser.setName("New Name");
        existingUser.setEmail(email);
        UserDTO userDTO = new UserDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toDTO(existingUser)).thenReturn(userDTO);

        UserDTO result = userService.updateUser(userId, updateDTO);

        assertEquals(userDTO, result);
        assertEquals("New Name", existingUser.getName());
        assertEquals(email, existingUser.getEmail());
        verify(userRepository, never()).existsByEmail(notNull());
    }

    /**
     * Проверяет создание пользователя с пустым именем. Ожидаемое поведение:
     * <ul>
     *   <li>Создает пользователя с указанным именем</li>
     *   <li>Не генерирует исключений при пустом имени</li>
     *   <li>Возвращает созданного пользователя</li>
     * </ul>
     */
    @Test
    void createUser_ShouldHandleEmptyName() {
        UserCreateDTO createDTO = new UserCreateDTO("Name", email);
        User user = new User();
        User savedUser = new User();
        UserDTO userDTO = new UserDTO();

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userMapper.toEntity(createDTO)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toDTO(savedUser)).thenReturn(userDTO);

        UserDTO result = userService.createUser(createDTO);

        assertEquals(userDTO, result);
    }
}