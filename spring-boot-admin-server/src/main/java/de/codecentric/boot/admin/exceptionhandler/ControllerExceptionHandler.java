// Project:   Deutsche Telekom - SPICA
// Author:    Josef Fuchshuber <josef.fuchshuber@qaware.de>
// Copyright: QAware GmbH

package de.codecentric.boot.admin.exceptionhandler;

import de.codecentric.boot.admin.exception.ApplicationNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler which handles exceptions thrown by {@link org.springframework.stereotype.Controller}.
 */
@ControllerAdvice
public final class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException exception, WebRequest request) {
        return handleExceptionInternal(exception, null, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<Object> handleApplicationNotFoundException(ApplicationNotFoundException exception, WebRequest request) {
        return handleExceptionInternal(exception, null, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    /**
     * A single place to customize the response body of all Exception types.
     * This method returns {@code null} by default.
     * @param ex the exception
     * @param body the body to use for the response
     * @param headers the headers to be written to the response
     * @param status the selected response status
     * @param request the current request
     */
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
                                                             HttpHeaders headers, HttpStatus status, WebRequest request) {

        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute("javax.servlet.error.exception", ex, WebRequest.SCOPE_REQUEST);
        }
        logExceptionAsInfo(ex, status);
        return new ResponseEntity<>(body, headers, status);
    }


    private static void logExceptionAsError(Throwable exception, HttpStatus statusCode){
        String exceptionMessage = String.format("Exception handled: %s, Message: %s, HttpCode: %d", exception.getClass().getName(), exception.getMessage(), statusCode.value());
        logger.error(exceptionMessage, exception);
    }

    private static void logExceptionAsWarning(Throwable exception, HttpStatus statusCode){
        String exceptionMessage = String.format("Exception handled: %s, Message: %s, HttpCode: %d", exception.getClass().getName(), exception.getMessage(), statusCode.value());
        logger.warn(exceptionMessage, exception);
    }

    private static void logExceptionAsInfo(Throwable exception, HttpStatus statusCode){
        String exceptionMessage = String.format("Exception handled: %s, Message: %s, HttpCode: %d", exception.getClass().getName(), exception.getMessage(), statusCode.value());
        logger.info(exceptionMessage, exception);
    }

    private static void logExceptionAsDebug(Throwable exception, HttpStatus statusCode){
        String exceptionMessage = String.format("Exception handled: %s, Message: %s, HttpCode: %d", exception.getClass().getName(), exception.getMessage(), statusCode.value());
        logger.debug(exceptionMessage, exception);
    }
}
