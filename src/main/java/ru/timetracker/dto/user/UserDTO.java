package ru.timetracker.dto.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
}
