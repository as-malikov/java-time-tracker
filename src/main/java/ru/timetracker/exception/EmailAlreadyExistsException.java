package ru.timetracker.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmailAlreadyExistsException extends RuntimeException {
    private static final Logger logger = LogManager.getLogger(EmailAlreadyExistsException.class);

    public EmailAlreadyExistsException(String email) {
        super("Email already exists: " + email);
        logger.warn("Attempt to use existing email: {}", email);

        if (logger.isDebugEnabled()) {
            logger.debug("Stack trace for email conflict: ", this);
        }
    }
}