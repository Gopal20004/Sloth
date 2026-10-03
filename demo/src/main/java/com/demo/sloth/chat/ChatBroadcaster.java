package com.demo.sloth.chat;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatBroadcaster {

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> subscribers =
            new ConcurrentHashMap<>();

    public SseEmitter subscribe(String gameSlug) {
        SseEmitter emitter = new SseEmitter(30L * 60 * 1000);
        var gameSubscribers = subscribers.computeIfAbsent(gameSlug, ignored -> new CopyOnWriteArrayList<>());
        gameSubscribers.add(emitter);
        Runnable remove = () -> gameSubscribers.remove(emitter);
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(ignored -> remove.run());
        try {
            emitter.send(SseEmitter.event().name("ready").data("connected"));
        } catch (IOException exception) {
            remove.run();
            emitter.completeWithError(exception);
        }
        return emitter;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void broadcast(ChatMessageCreated event) {
        var gameSubscribers = subscribers.get(event.message().gameSlug());
        if (gameSubscribers == null) {
            return;
        }
        for (SseEmitter emitter : gameSubscribers) {
            try {
                emitter.send(SseEmitter.event().name("message")
                        .id(event.message().id().toString()).data(event.message()));
            } catch (IOException exception) {
                gameSubscribers.remove(emitter);
                emitter.completeWithError(exception);
            }
        }
    }

    @Scheduled(fixedRate = 25_000)
    public void keepConnectionsAlive() {
        subscribers.values().forEach(gameSubscribers -> {
            for (SseEmitter emitter : gameSubscribers) {
                try {
                    emitter.send(SseEmitter.event().comment("keepalive"));
                } catch (IOException exception) {
                    gameSubscribers.remove(emitter);
                    emitter.completeWithError(exception);
                }
            }
        });
    }
}
