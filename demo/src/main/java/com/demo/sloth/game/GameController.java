package com.demo.sloth.game;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

    @GetMapping("/popular")
    public PageResponse<GameResponse> popular(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return service.popular(page, size);
    }

    @GetMapping("/search")
    public PageResponse<GameResponse> search(
            @RequestParam("q") @NotBlank @Size(max = 100) String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return service.search(query, page, size);
    }

    @GetMapping("/{slug}")
    public GameResponse getBySlug(@PathVariable String slug) {
        return service.getBySlug(slug);
    }
}
