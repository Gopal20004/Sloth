package com.demo.sloth.chat;

import com.demo.sloth.game.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/games/{slug}/chat")
public class ChatController {

    private final ChatService service;
    private final ChatBroadcaster broadcaster;

    public ChatController(ChatService service, ChatBroadcaster broadcaster) {
        this.service = service;
        this.broadcaster = broadcaster;
    }

    @GetMapping("/messages")
    public PageResponse<ChatMessageResponse> recent(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size
    ) {
        return service.recent(slug, page, size);
    }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageResponse send(@PathVariable String slug, @AuthenticationPrincipal Long userId,
                                    @Valid @RequestBody SendMessageRequest request) {
        return service.send(slug, userId, request);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String slug) {
        service.requireGame(slug);
        return broadcaster.subscribe(slug);
    }
}
