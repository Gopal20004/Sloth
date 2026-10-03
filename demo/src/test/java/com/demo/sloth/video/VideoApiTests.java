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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class VideoApiTests {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    @Autowired AuthService auth;

    @Test
    void uploadBrowsePlayAndDeleteClip() throws Exception {
        String owner = newUserToken();
        String stranger = newUserToken();
        byte[] mp4 = {0, 0, 0, 16, 'f', 't', 'y', 'p', 'i', 's', 'o', 'm', 0, 0, 0, 0};
        MockMultipartFile file = new MockMultipartFile("file", "clip.mp4", "video/mp4", mp4);

        String uploadBody = mvc.perform(multipart("/api/videos")
                        .file(file)
                        .param("title", "My best match")
                        .param("gameSlug", "minecraft")
                        .header("Authorization", owner))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameSlug").value("minecraft"))
                .andReturn().getResponse().getContentAsString();
        int videoId = JsonPath.read(uploadBody, "$.id");
        int ownerId = JsonPath.read(uploadBody, "$.ownerId");

        mvc.perform(get("/api/games/minecraft/videos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(videoId));
        mvc.perform(get("/api/users/{id}/videos", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(videoId));
        mvc.perform(get("/api/videos/{id}/file", videoId))
                .andExpect(status().isOk())
                .andExpect(content().bytes(mp4));

        mvc.perform(delete("/api/videos/{id}", videoId).header("Authorization", stranger))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/videos/{id}", videoId).header("Authorization", owner))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/videos/{id}", videoId))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsNonVideoUpload() throws Exception {
        String owner = newUserToken();
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain", "not video".getBytes());
        mvc.perform(multipart("/api/videos")
                        .file(file)
                        .param("title", "Bad file")
                        .header("Authorization", owner))
                .andExpect(status().isUnsupportedMediaType());
    }

    private String newUserToken() {
        String email = UUID.randomUUID() + "@example.com";
        users.save(new User("Player", email, passwords.encode("secret-password")));
        return "Bearer " + auth.login(new LoginRequest(email, "secret-password")).token();
    }
}
