package ru.timetracker.service;

import org.springframework.stereotype.Service;
import ru.timetracker.repository.User;
import ru.timetracker.repository.UserRepository;
import ru.timetracker.repository.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Возвращает список всех зарегистрированных пользователей системы.
     * <p>
     * Возвращаемый список включает как активных, так и неактивных пользователей.
     * Для больших наборов данных рекомендуется использовать пагинацию.
     *
     * @return список всех пользователей (не может быть null, может быть пустым)
     * @see UserRepository#findAll()
     * @since 1.0
     */
    public List<User> get() {
        return userRepository.findAll();
    }

    /**
     * Возвращает пользователя по указанному идентификатору.
     * <p>
     * Метод выполняет поиск пользователя в репозитории по ID. Если пользователь не найден,
     * выбрасывает исключение {@link IllegalStateException}.
     *
     * @param id идентификатор пользователя (не может быть null, должен быть > 0)
     * @return найденный объект пользователя (never null)
     * @throws IllegalStateException если пользователь с указанным ID не найден
     * @throws IllegalArgumentException если параметр id равен null или некорректный
     * @see UserRepository#findById(Object)
     * @since 1.0
     */
    public User getById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new IllegalStateException("User with id = " + id + " not found");
        }
        User user = optionalUser.get();
        return user;
    }

    /**
     * Создает нового пользователя с проверкой уникальности email и login.
     * Устанавливает роль USER по умолчанию, активирует аккаунт и временные метки.
     *
     * @param user объект пользователя (не null)
     * @return сохраненный пользователь
     * @throws IllegalStateException если email/login уже существуют
     * @throws IllegalArgumentException при невалидных данных
     * @since 1.0
     */
    public User create(User user) {
        Optional<User> foundUserByEmail = userRepository.findByEmail(user.getEmail());
        if (foundUserByEmail.isPresent()) {
            throw new IllegalStateException("User with email " + user.getEmail() + " already exists");
        }
        Optional<User> foundUserByLogin = userRepository.findByLogin(user.getLogin());
        if (foundUserByLogin.isPresent()) {
            throw new IllegalStateException("User with login " + user.getLogin() + " already exists");
        }
        if (user.getRole() == null) {
            user.setRole(UserRole.USER);
        }
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Удаляет пользователя с указанным идентификатором из системы.
     * <p>
     * Перед удалением проверяет существование пользователя. Если пользователь не найден,
     * выбрасывает исключение. В случае успешного выполнения не возвращает значение.
     *
     * @param id идентификатор пользователя для удаления (должен быть > 0)
     * @throws IllegalStateException если пользователь с указанным id не найден
     * @throws IllegalArgumentException если id равен null
     * @see UserRepository#deleteById(Object)
     * @since 1.0
     */
    public void delete(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new IllegalStateException("User with id = " + id + " not found");
        }
        userRepository.deleteById(id);
    }

    /**
     * Обновляет данные пользователя с указанным идентификатором.
     * Метод выполняет частичное обновление - изменяет только переданные не-null значения.
     * Проверяет уникальность email и login перед обновлением.
     *
     * @param id      идентификатор пользователя для обновления (должен быть > 0)
     * @param newUser объект с новыми данными пользователя (не может быть null)
     * @return обновленный объект пользователя
     * @throws IllegalStateException    если:
     *                                  - пользователь с указанным id не найден
     *                                  - новый email уже существует у другого пользователя
     *                                  - новый login уже существует у другого пользователя
     * @throws IllegalArgumentException если id или newUser равны null
     * @see User
     * @since 1.0
     */
    public User update(Long id, User newUser) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new IllegalStateException("User with id = " + id + " not found");
        }
        User user = optionalUser.get();

        if (newUser.getEmail() != null && !newUser.getEmail()
                .equals(user.getEmail()))
        {
            Optional<User> foundUserByEmail = userRepository.findByEmail(newUser.getEmail());
            if (foundUserByEmail.isPresent()) {
                throw new IllegalStateException("User with email " + newUser.getEmail() + " already exists");
            }
            user.setEmail(newUser.getEmail());
        }
        if (newUser.getLogin() != null && !newUser.getLogin()
                .equals(user.getLogin()))
        {
            Optional<User> foundUserByLogin = userRepository.findByLogin(newUser.getLogin());
            if (foundUserByLogin.isPresent()) {
                throw new IllegalStateException("User with login " + newUser.getLogin() + " already exists");
            }
            user.setLogin(newUser.getLogin());
        }
        if (!newUser.getPassword()
                .equals(user.getPassword()))
        {
            user.setPassword(newUser.getPassword());
        }
        if (!newUser.getFirstName()
                .equals(user.getFirstName()))
        {
            user.setFirstName(newUser.getFirstName());
        }
        if (!newUser.getLastName()
                .equals(user.getLastName()))
        {
            user.setLastName(newUser.getLastName());
        }

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}