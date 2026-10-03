package com.demo.sloth.server;

import com.demo.sloth.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "server_members", uniqueConstraints = @UniqueConstraint(columnNames = {"server_id", "user_id"}))
public class ServerMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id")
    private PlayerServer server;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    protected ServerMember() {
    }

    public ServerMember(PlayerServer server, User user) {
        this.server = server;
        this.user = user;
    }

    @PrePersist
    void setJoinedAt() {
        joinedAt = Instant.now();
    }

    public User getUser() { return user; }
    public PlayerServer getServer() { return server; }
    public Instant getJoinedAt() { return joinedAt; }
}
