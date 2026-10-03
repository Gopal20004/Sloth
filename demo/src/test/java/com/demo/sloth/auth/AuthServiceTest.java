package com.demo.sloth.auth;

import com.demo.sloth.user.User;
import com.demo.sloth.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock AuthSessionRepository sessionRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks AuthService authService;

    @Test
    void loginNormalizesEmailAndStoresOnlyTokenHash() {
        User user = new User("Player", "player@example.com", "stored-hash");
        when(userRepository.findByEmail("player@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("good-password", "stored-hash")).thenReturn(true);

        LoginResponse response = authService.login(new LoginRequest("PLAYER@EXAMPLE.COM", "good-password"));

        ArgumentCaptor<AuthSession> session = ArgumentCaptor.forClass(AuthSession.class);
        verify(sessionRepository).save(session.capture());
        assertEquals(43, response.token().length());
        assertEquals(AuthService.hash(response.token()), session.getValue().getTokenHash());
        assertNotEquals(response.token(), session.getValue().getTokenHash());
        assertTrue(response.expiresAt().isAfter(Instant.now().plusSeconds(29L * 24 * 60 * 60)));
    }

    @Test
    void wrongPasswordNeverCreatesSession() {
        User user = new User("Player", "player@example.com", "stored-hash");
        when(userRepository.findByEmail("player@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "stored-hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("player@example.com", "wrong-password")));
        verify(sessionRepository, never()).save(any());
    }
}
