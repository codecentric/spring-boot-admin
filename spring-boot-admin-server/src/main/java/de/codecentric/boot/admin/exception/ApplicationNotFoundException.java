package de.codecentric.boot.admin.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Project:   spring-boot-admin
 * Copyright: Deutsche Telekom AG
 *
 * @author Robert Winkler <robert.winkler@telekom.de>
 * @since 2.0.0
 */
@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="No such application registered")  // 404
public class ApplicationNotFoundException extends RuntimeException{

    public ApplicationNotFoundException(String message) {
        super(message);
    }
}
