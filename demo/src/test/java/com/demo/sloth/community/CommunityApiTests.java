package com.demo.sloth;

import com.demo.sloth.auth.AuthService;
import com.demo.sloth.auth.LoginRequest;
import com.demo.sloth.user.User;
import com.demo.sloth.user.UserRepository;
import com.jayway.jsonpath.JsonPath;
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
class CommunityApiTests {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    @Autowired AuthService auth;

    @Test
    void discussionIsPublicToReadAndAuthorsControlDeletion() throws Exception {
        String author = newUserToken();
        String visitor = newUserToken();

        mvc.perform(post("/api/games/minecraft/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Building tips\",\"body\":\"Share your builds\"}"))
                .andExpect(status().isUnauthorized());

        String postBody = mvc.perform(post("/api/games/minecraft/posts")
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Building tips\",\"body\":\"Share your builds\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameSlug").value("minecraft"))
                .andReturn().getResponse().getContentAsString();
        int postId = JsonPath.read(postBody, "$.id");

        mvc.perform(get("/api/games/minecraft/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Building tips"));

        String replyBody = mvc.perform(post("/api/posts/{id}/replies", postId)
                        .header("Authorization", visitor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"Try a mountain base\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int replyId = JsonPath.read(replyBody, "$.id");

        mvc.perform(get("/api/posts/{id}/replies", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].body").value("Try a mountain base"));

        mvc.perform(delete("/api/posts/{id}", postId).header("Authorization", visitor))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/replies/{id}", replyId).header("Authorization", visitor))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/posts/{id}", postId).header("Authorization", author))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/posts/{id}", postId))
                .andExpect(status().isNotFound());
    }

    private String newUserToken() {
        String email = UUID.randomUUID() + "@example.com";
        users.save(new User("Player", email, passwords.encode("secret-password")));
        return "Bearer " + auth.login(new LoginRequest(email, "secret-password")).token();
    }
}
