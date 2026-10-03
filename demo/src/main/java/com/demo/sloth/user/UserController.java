package com.demo.sloth.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        return userService.register(request);
    }

    @GetMapping("/me")
    public UserResponse getProfile(@AuthenticationPrincipal Long userId) {
        return userService.getProfile(userId);
    }

    @PatchMapping("/me")
    public UserResponse updateProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return userService.updateProfile(userId, request);
    }
}
