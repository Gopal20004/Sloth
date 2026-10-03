package com.demo.sloth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class GameApiTests {

    @Autowired MockMvc mvc;

    @Test
    void publicCatalogSupportsPopularSearchAndDetail() throws Exception {
        mvc.perform(get("/api/games/popular").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].slug").value("minecraft"))
                .andExpect(jsonPath("$.totalElements").value(8));

        mvc.perform(get("/api/games/search").param("q", "MINE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].slug").value("minecraft"))
                .andExpect(jsonPath("$.totalElements").value(1));

        mvc.perform(get("/api/games/minecraft"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Minecraft"));

        mvc.perform(get("/api/games/unknown-game"))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidPageSizeIsRejected() throws Exception {
        mvc.perform(get("/api/games/popular").param("size", "0"))
                .andExpect(status().isBadRequest());
    }
}
