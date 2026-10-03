package com.demo.sloth.config;

import com.demo.sloth.auth.InvalidCredentialsException;
import com.demo.sloth.user.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
    public Map<String, String> handleVideoTooLarge(MaxUploadSizeExceededException exception) {
        return Map.of("error", "Video exceeds 100 MB");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleInvalidCredentials(InvalidCredentialsException exception) {
        return Map.of("error", exception.getMessage());
    }

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
