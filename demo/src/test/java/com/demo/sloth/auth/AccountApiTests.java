package com.demo.sloth;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AccountApiTests {

    @Autowired MockMvc mvc;

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost:5173", "http://127.0.0.1:5173"})
    void localBrowserOriginsCanPreflightAndReachLogin(String origin) throws Exception {
        mvc.perform(options("/api/auth/login")
                        .header("Origin", origin)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type, Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", origin));

        mvc.perform(post("/api/auth/login")
                        .header("Origin", origin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"missing-cors-user@example.com\",\"password\":\"invalid-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", origin));
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://untrusted.example", "http://localhost:5174"})
    void otherOriginsCannotPreflightOrLogin(String origin) throws Exception {
        mvc.perform(options("/api/auth/login")
                        .header("Origin", origin)
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));

        mvc.perform(post("/api/auth/login")
                        .header("Origin", origin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"missing-cors-user@example.com\",\"password\":\"invalid-password\"}"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void registrationLoginProfileAndLogout() throws Exception {
        String email = "player-" + UUID.randomUUID() + "@example.com";

        mvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());

        String registrationBody = mvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":" Player ","email":"%s","password":"secret-password"}
                                """.formatted(email.toUpperCase())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.displayName").value("Player"))
                .andReturn().getResponse().getContentAsString();

        Number userId = JsonPath.read(registrationBody, "$.id");
        mvc.perform(get("/api/users/public/{id}", userId.longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Player"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"wrong-password"}
                                """.formatted(email)))
                .andExpect(status().isUnauthorized());

        String loginBody = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"secret-password"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresAt").exists())
                .andReturn().getResponse().getContentAsString();
        String authorization = "Bearer " + JsonPath.read(loginBody, "$.token");

        mvc.perform(get("/api/users/me").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        mvc.perform(patch("/api/users/me")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"New name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("New name"));

        mvc.perform(delete("/api/auth/logout").header("Authorization", authorization))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/users/me").header("Authorization", authorization))
                .andExpect(status().isUnauthorized());
    }
}
