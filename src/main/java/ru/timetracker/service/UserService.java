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

    public List<User> get() {
        return userRepository.findAll();
    }

    public User create(User user) {
        Optional<User> foundUserByEmail = userRepository.findByEmail(user.getEmail());
        if (foundUserByEmail.isPresent()) {
            throw new IllegalStateException("User with email " + user.getEmail() + " already exists");
        }
        Optional<User> foundUserByLogin = userRepository.findByLogin(user.getLogin());
        if (foundUserByLogin.isPresent()) {
            throw new IllegalStateException("User with login " + user.getLogin() + " already exists");
        }
        if (user.getPassword() == null || user.getPassword()
                .isEmpty())
        {
            throw new IllegalStateException("Password is empty");
        }
        if (user.getRole() == null) {
            user.setRole(UserRole.USER);
        }
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public void delete(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new IllegalStateException("User with id = " + id + " not found");
        }
        userRepository.deleteById(id);
    }

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
        if (newUser.getLogin() != null && !newUser.getLogin().equals(user.getLogin()))
        {
            Optional<User> foundUserByLogin = userRepository.findByLogin(newUser.getLogin());
            if (foundUserByLogin.isPresent()) {
                throw new IllegalStateException("User with login " + newUser.getLogin() + " already exists");
            }
            user.setLogin(newUser.getLogin());
        }
        return userRepository.save(user);
    }
}
