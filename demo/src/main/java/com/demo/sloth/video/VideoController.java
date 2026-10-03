package com.demo.sloth.video;

import com.demo.sloth.game.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class VideoController {

    private final VideoService service;

    public VideoController(VideoService service) {
        this.service = service;
    }

    @PostMapping(value = "/videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public VideoResponse upload(@AuthenticationPrincipal Long userId,
                                @RequestParam String title,
                                @RequestParam(required = false) String gameSlug,
                                @RequestParam("file") MultipartFile file) {
        return service.upload(userId, title, gameSlug, file);
    }

    @GetMapping("/games/{slug}/videos")
    public PageResponse<VideoResponse> forGame(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return service.forGame(slug, page, size);
    }

    @GetMapping("/users/{userId}/videos")
    public PageResponse<VideoResponse> forUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return service.forUser(userId, page, size);
    }

    @GetMapping("/videos/{id}")
    public VideoResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/videos/{id}/file")
    public ResponseEntity<Resource> file(@PathVariable Long id) {
        VideoService.VideoFile file = service.file(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.sizeBytes())
                .body(file.resource());
    }

    @DeleteMapping("/videos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        service.delete(id, userId);
    }
}
