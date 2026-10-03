package com.demo.sloth.video;

import com.demo.sloth.game.Game;
import com.demo.sloth.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(name = "storage_key", nullable = false, unique = true, length = 50)
    private String storageKey;

    @Column(name = "content_type", nullable = false, length = 20)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Video() {
    }

    public Video(User owner, Game game, String title, String storageKey, String contentType, long sizeBytes) {
        this.owner = owner;
        this.game = game;
        this.title = title;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }

    @PrePersist
    void setCreatedAt() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getOwner() { return owner; }
    public Game getGame() { return game; }
    public String getTitle() { return title; }
    public String getStorageKey() { return storageKey; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public Instant getCreatedAt() { return createdAt; }
}
