package com.demo.sloth.server;

import com.demo.sloth.game.PageResponse;
import com.demo.sloth.user.User;
import com.demo.sloth.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class ServerService {

    private final PlayerServerRepository servers;
    private final ServerMemberRepository members;
    private final ServerInviteRepository invites;
    private final UserRepository users;

    public ServerService(PlayerServerRepository servers, ServerMemberRepository members,
                         ServerInviteRepository invites, UserRepository users) {
        this.servers = servers;
        this.members = members;
        this.invites = invites;
        this.users = users;
    }

    @Transactional
    public ServerResponse create(Long ownerId, CreateServerRequest request) {
        User owner = users.findById(ownerId).orElseThrow();
        String description = request.description() == null ? "" : request.description().trim();
        PlayerServer server = servers.save(new PlayerServer(request.name().trim(), description, owner));
        members.save(new ServerMember(server, owner));
        return ServerResponse.from(server);
    }

    @Transactional(readOnly = true)
    public PageResponse<ServerResponse> mine(Long userId, int page, int size) {
        return PageResponse.from(members.findByUserIdOrderByJoinedAtDesc(userId,
                PageRequest.of(page, size)).map(member -> ServerResponse.from(member.getServer())));
    }

    @Transactional(readOnly = true)
    public ServerResponse get(Long serverId, Long userId) {
        requireMember(serverId, userId);
        return ServerResponse.from(findServer(serverId));
    }

    @Transactional(readOnly = true)
    public PageResponse<ServerMemberResponse> listMembers(Long serverId, Long userId, int page, int size) {
        requireMember(serverId, userId);
        return PageResponse.from(members.findByServerIdOrderByJoinedAtAsc(serverId,
                PageRequest.of(page, size)).map(ServerMemberResponse::from));
    }

    @Transactional
    public InviteResponse createInvite(Long serverId, Long ownerId) {
        PlayerServer server = findServer(serverId);
        requireOwner(server, ownerId);
        String code = InviteCode.generate();
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);
        ServerInvite invite = invites.save(new ServerInvite(server, InviteCode.hash(code), expiresAt));
        return new InviteResponse(invite.getId(), code, expiresAt);
    }

    @Transactional
    public void revokeInvite(Long serverId, Long inviteId, Long ownerId) {
        requireOwner(findServer(serverId), ownerId);
        ServerInvite invite = invites.findById(inviteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite not found"));
        if (!invite.getServer().getId().equals(serverId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite not found");
        }
        invites.delete(invite);
    }

    @Transactional
    public ServerResponse join(Long userId, JoinServerRequest request) {
        ServerInvite invite = invites.findByCodeHashAndExpiresAtAfter(
                        InviteCode.hash(request.code().trim()), Instant.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite not found"));
        PlayerServer server = invite.getServer();
        if (!members.existsByServerIdAndUserId(server.getId(), userId)) {
            User user = users.findById(userId).orElseThrow();
            members.save(new ServerMember(server, user));
        }
        return ServerResponse.from(server);
    }

    @Transactional
    public void leave(Long serverId, Long userId) {
        PlayerServer server = findServer(serverId);
        if (server.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Owner must delete the server");
        }
        ServerMember member = members.findByServerIdAndUserId(serverId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found"));
        members.delete(member);
    }

    @Transactional
    public void removeMember(Long serverId, Long targetUserId, Long ownerId) {
        PlayerServer server = findServer(serverId);
        requireOwner(server, ownerId);
        if (server.getOwner().getId().equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Owner cannot be removed");
        }
        ServerMember member = members.findByServerIdAndUserId(serverId, targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found"));
        members.delete(member);
    }

    @Transactional
    public void delete(Long serverId, Long ownerId) {
        PlayerServer server = findServer(serverId);
        requireOwner(server, ownerId);
        servers.delete(server);
    }

    private PlayerServer findServer(Long id) {
        return servers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Server not found"));
    }

    private void requireMember(Long serverId, Long userId) {
        if (!members.existsByServerIdAndUserId(serverId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Server membership required");
        }
    }

    private void requireOwner(PlayerServer server, Long userId) {
        if (!server.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Server owner required");
        }
    }
}
