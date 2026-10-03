package com.demo.sloth.server;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "server_invites")
public class ServerInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id")
    private PlayerServer server;

    @Column(name = "code_hash", nullable = false, unique = true, length = 64)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected ServerInvite() {
    }

    public ServerInvite(PlayerServer server, String codeHash, Instant expiresAt) {
        this.server = server;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public PlayerServer getServer() { return server; }
    public Instant getExpiresAt() { return expiresAt; }
}
