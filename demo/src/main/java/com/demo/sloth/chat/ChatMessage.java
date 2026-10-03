package com.demo.sloth.chat;

import com.demo.sloth.game.Game;
import com.demo.sloth.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(nullable = false, length = 1000)
    private String body;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt;

    protected ChatMessage() {
    }

    public ChatMessage(Game game, User sender, String body) {
        this.game = game;
        this.sender = sender;
        this.body = body;
    }

    @PrePersist
    void setSentAt() {
        sentAt = Instant.now();
    }

    public Long getId() { return id; }
    public Game getGame() { return game; }
    public User getSender() { return sender; }
    public String getBody() { return body; }
    public Instant getSentAt() { return sentAt; }
}
