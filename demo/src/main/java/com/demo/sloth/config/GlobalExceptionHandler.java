package com.demo.sloth.config;

import com.demo.sloth.user.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleUserAlreadyExists(
            UserAlreadyExistsException exception
    ) {
        return Map.of(
                "error", exception.getMessage()
        );
    }
}