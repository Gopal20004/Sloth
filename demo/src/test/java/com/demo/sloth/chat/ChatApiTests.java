package com.demo.sloth;

import com.demo.sloth.auth.AuthService;
import com.demo.sloth.auth.LoginRequest;
import com.demo.sloth.user.User;
import com.demo.sloth.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ChatApiTests {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    @Autowired AuthService auth;

    @Test
    void chatPersistsMessagesAndOffersLiveStream() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        users.save(new User("Chatter", email, passwords.encode("secret-password")));
        String authorization = "Bearer " + auth.login(new LoginRequest(email, "secret-password")).token();
        String body = "hello-" + UUID.randomUUID();

        mvc.perform(get("/api/games/minecraft/chat/stream")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));

        mvc.perform(post("/api/games/minecraft/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"hello\"}"))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/games/minecraft/chat/messages")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"" + body + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body").value(body))
                .andExpect(jsonPath("$.senderName").value("Chatter"));

        mvc.perform(get("/api/games/minecraft/chat/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].body").value(body));
    }
}
