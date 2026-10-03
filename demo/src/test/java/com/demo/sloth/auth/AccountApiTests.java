package com.demo.sloth;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
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

    @Test
    void registrationLoginProfileAndLogout() throws Exception {
        String email = "player-" + UUID.randomUUID() + "@example.com";

        mvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":" Player ","email":"%s","password":"secret-password"}
                                """.formatted(email.toUpperCase())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.displayName").value("Player"));

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
