package ru.timetracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.timetracker.repository.User;

import java.util.List;

@RestController
public class UserController {
    @GetMapping
    public List<User> mainPage() {
        return List.of(
                new User(1L, "User1", "user1@mail.ru"),
                new User(2L, "User2", "user2@mail.ru"),
                new User(3L, "User3", "user3@mail.ru")
        );
    }
}
