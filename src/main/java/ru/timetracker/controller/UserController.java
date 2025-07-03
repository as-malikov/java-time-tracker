package ru.timetracker.controller;

import org.springframework.web.bind.annotation.*;
import ru.timetracker.repository.User;
import ru.timetracker.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.get();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.create(user);
    }

    @DeleteMapping(path = "{id}")
    public void deleteUser(@PathVariable(name = "id") Long id) {
        userService.delete(id);
    }

    @PutMapping(path = "{id}")
    public User updateUser(@PathVariable(name = "id") Long id, @RequestBody User user) {
        return userService.update(id, user);
    }
}
