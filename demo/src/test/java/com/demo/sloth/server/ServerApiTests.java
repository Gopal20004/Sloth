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
class ServerApiTests {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    @Autowired AuthService auth;

    @Test
    void invitesControlMembershipAndOwnerActions() throws Exception {
        String owner = newUserToken();
        String guest = newUserToken();
        String outsider = newUserToken();

        String serverBody = mvc.perform(post("/api/servers")
                        .header("Authorization", owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Our crew\",\"description\":\"Co-op players\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Our crew"))
                .andReturn().getResponse().getContentAsString();
        int serverId = JsonPath.read(serverBody, "$.id");

        mvc.perform(get("/api/servers/{id}", serverId).header("Authorization", outsider))
                .andExpect(status().isForbidden());

        String inviteBody = mvc.perform(post("/api/servers/{id}/invites", serverId)
                        .header("Authorization", owner))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expiresAt").exists())
                .andReturn().getResponse().getContentAsString();
        int inviteId = JsonPath.read(inviteBody, "$.id");
        String code = JsonPath.read(inviteBody, "$.code");

        mvc.perform(post("/api/servers/join")
                        .header("Authorization", guest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(serverId));

        mvc.perform(get("/api/servers/{id}/members", serverId).header("Authorization", guest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(post("/api/servers/{id}/invites", serverId).header("Authorization", guest))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/servers/{id}/members/me", serverId).header("Authorization", owner))
                .andExpect(status().isConflict());

        mvc.perform(delete("/api/servers/{id}/invites/{inviteId}", serverId, inviteId)
                        .header("Authorization", owner))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/servers/join")
                        .header("Authorization", outsider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\"}"))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/servers/{id}/members/me", serverId).header("Authorization", guest))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/servers/{id}", serverId).header("Authorization", owner))
                .andExpect(status().isNoContent());
    }

    private String newUserToken() {
        String email = UUID.randomUUID() + "@example.com";
        users.save(new User("Player", email, passwords.encode("secret-password")));
        return "Bearer " + auth.login(new LoginRequest(email, "secret-password")).token();
    }
}
