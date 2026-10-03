package com.demo.sloth.community;

import com.demo.sloth.game.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CommunityController {

    private final CommunityService service;

    public CommunityController(CommunityService service) {
        this.service = service;
    }

    @GetMapping("/games/{slug}/posts")
    public PageResponse<PostResponse> listPosts(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return service.listPosts(slug, page, size);
    }

    @PostMapping("/games/{slug}/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createPost(@PathVariable String slug, @AuthenticationPrincipal Long userId,
                                   @Valid @RequestBody CreatePostRequest request) {
        return service.createPost(slug, userId, request);
    }

    @GetMapping("/posts/{id}")
    public PostResponse getPost(@PathVariable Long id) {
        return service.getPost(id);
    }

    @DeleteMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        service.deletePost(id, userId);
    }

    @GetMapping("/posts/{id}/replies")
    public PageResponse<ReplyResponse> listReplies(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return service.listReplies(id, page, size);
    }

    @PostMapping("/posts/{id}/replies")
    @ResponseStatus(HttpStatus.CREATED)
    public ReplyResponse createReply(@PathVariable Long id, @AuthenticationPrincipal Long userId,
                                     @Valid @RequestBody CreateReplyRequest request) {
        return service.createReply(id, userId, request);
    }

    @DeleteMapping("/replies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReply(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        service.deleteReply(id, userId);
    }
}
