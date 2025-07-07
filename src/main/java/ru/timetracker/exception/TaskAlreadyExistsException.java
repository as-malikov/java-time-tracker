package ru.timetracker.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TaskAlreadyExistsException extends RuntimeException {
    private static final Logger logger = LogManager.getLogger(TaskAlreadyExistsException.class);

    public TaskAlreadyExistsException(String title) {
        super("Task with title '" + title + "' already exists");
        logError(title);
    }

    private void logError(String identifier) {
        logger.warn("Attempt to create duplicate task: {}", identifier);

        if (logger.isDebugEnabled()) {
            logger.debug("Duplicate task creation attempt details:", this);
        }
    }
}