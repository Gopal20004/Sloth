package com.demo.sloth.server;

import com.demo.sloth.game.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/servers")
public class ServerController {

    private final ServerService service;

    public ServerController(ServerService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServerResponse create(@AuthenticationPrincipal Long userId,
                                 @Valid @RequestBody CreateServerRequest request) {
        return service.create(userId, request);
    }

    @GetMapping("/mine")
    public PageResponse<ServerResponse> mine(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return service.mine(userId, page, size);
    }

    @PostMapping("/join")
    public ServerResponse join(@AuthenticationPrincipal Long userId,
                               @Valid @RequestBody JoinServerRequest request) {
        return service.join(userId, request);
    }

    @GetMapping("/{id}")
    public ServerResponse get(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return service.get(id, userId);
    }

    @GetMapping("/{id}/members")
    public PageResponse<ServerMemberResponse> listMembers(
            @PathVariable Long id, @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return service.listMembers(id, userId, page, size);
    }

    @PostMapping("/{id}/invites")
    @ResponseStatus(HttpStatus.CREATED)
    public InviteResponse createInvite(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return service.createInvite(id, userId);
    }

    @DeleteMapping("/{id}/invites/{inviteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeInvite(@PathVariable Long id, @PathVariable Long inviteId,
                             @AuthenticationPrincipal Long userId) {
        service.revokeInvite(id, inviteId, userId);
    }

    @DeleteMapping("/{id}/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        service.leave(id, userId);
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable Long id, @PathVariable Long memberId,
                             @AuthenticationPrincipal Long userId) {
        service.removeMember(id, memberId, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        service.delete(id, userId);
    }
}
