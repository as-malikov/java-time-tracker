package ru.timetracker.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResourceNotFoundException extends RuntimeException {
    private static final Logger logger = LogManager.getLogger(ResourceNotFoundException.class);

    public ResourceNotFoundException(String message) {
        super(message);
        logger.warn("Resource not found: {}", message);
    }
}