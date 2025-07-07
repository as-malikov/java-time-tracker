package ru.timetracker.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskAlreadyExistsException extends RuntimeException {
    public TaskAlreadyExistsException(String title) {
        super(title);
        log.warn("Attempt to create duplicate task with title: {}", title);
    }
}
