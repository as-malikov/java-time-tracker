package ru.timetracker.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Исключение, выбрасываемое при попытке регистрации или обновления email,
 * который уже существует в системе.
 *
 * <p>Особенности:
 * <ul>
 *   <li>Логирует предупреждение о попытке использования существующего email</li>
 *   <li>В debug-режиме сохраняет stack trace</li>
 *   <li>Содержит понятное сообщение об ошибке для клиента</li>
 * </ul>
 */
public class EmailAlreadyExistsException extends RuntimeException {
    private static final Logger logger = LogManager.getLogger(EmailAlreadyExistsException.class);

    /**
     * Создает исключение с указанием проблемного email
     *
     * @param email Email, который уже существует в системе
     */
    public EmailAlreadyExistsException(String email) {
        super("Email already exists: " + email);
        logger.warn("Attempt to use existing email: {}", email);

        if (logger.isDebugEnabled()) {
            logger.debug("Stack trace for email conflict: ", this);
        }
    }
}