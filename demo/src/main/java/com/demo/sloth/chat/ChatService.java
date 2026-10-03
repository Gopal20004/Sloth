package com.demo.sloth.chat;

import com.demo.sloth.game.Game;
import com.demo.sloth.game.GameRepository;
import com.demo.sloth.game.PageResponse;
import com.demo.sloth.user.User;
import com.demo.sloth.user.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChatService {

    private final GameRepository games;
    private final UserRepository users;
    private final ChatMessageRepository messages;
    private final ApplicationEventPublisher events;

    public ChatService(GameRepository games, UserRepository users, ChatMessageRepository messages,
                       ApplicationEventPublisher events) {
        this.games = games;
        this.users = users;
        this.messages = messages;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public PageResponse<ChatMessageResponse> recent(String gameSlug, int page, int size) {
        findGame(gameSlug);
        return PageResponse.from(messages.findByGameSlugOrderBySentAtDesc(gameSlug,
                PageRequest.of(page, size)).map(ChatMessageResponse::from));
    }

    @Transactional
    public ChatMessageResponse send(String gameSlug, Long senderId, SendMessageRequest request) {
        Game game = findGame(gameSlug);
        User sender = users.findById(senderId).orElseThrow();
        ChatMessage message = messages.save(new ChatMessage(game, sender, request.body().trim()));
        ChatMessageResponse response = ChatMessageResponse.from(message);
        events.publishEvent(new ChatMessageCreated(response));
        return response;
    }

    @Transactional(readOnly = true)
    public void requireGame(String slug) {
        findGame(slug);
    }

    private Game findGame(String slug) {
        return games.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }
}
