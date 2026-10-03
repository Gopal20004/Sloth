package com.demo.sloth.video;

import com.demo.sloth.game.Game;
import com.demo.sloth.game.GameRepository;
import com.demo.sloth.game.PageResponse;
import com.demo.sloth.user.User;
import com.demo.sloth.user.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VideoService {

    private final VideoRepository videos;
    private final UserRepository users;
    private final GameRepository games;
    private final VideoStorage storage;

    public VideoService(VideoRepository videos, UserRepository users, GameRepository games, VideoStorage storage) {
        this.videos = videos;
        this.users = users;
        this.games = games;
        this.storage = storage;
    }

    @Transactional
    public VideoResponse upload(Long ownerId, String title, String gameSlug, MultipartFile file) {
        if (title == null || title.isBlank() || title.length() > 160) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title must be 1–160 characters");
        }
        User owner = users.findById(ownerId).orElseThrow();
        Game game = null;
        if (gameSlug != null) {
            if (gameSlug.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game slug is blank");
            }
            game = games.findBySlug(gameSlug)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        }

        VideoStorage.StoredFile stored = storage.save(file);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    storage.deleteQuietly(stored.key());
                }
            }
        });
        Video video = videos.save(new Video(owner, game, title.trim(), stored.key(),
                stored.contentType(), stored.sizeBytes()));
        return VideoResponse.from(video);
    }

    @Transactional(readOnly = true)
    public PageResponse<VideoResponse> forGame(String slug, int page, int size) {
        games.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        return PageResponse.from(videos.findByGameSlugOrderByCreatedAtDesc(slug,
                PageRequest.of(page, size)).map(VideoResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<VideoResponse> forUser(Long userId, int page, int size) {
        if (!users.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return PageResponse.from(videos.findByOwnerIdOrderByCreatedAtDesc(userId,
                PageRequest.of(page, size)).map(VideoResponse::from));
    }

    @Transactional(readOnly = true)
    public VideoResponse get(Long id) {
        return VideoResponse.from(findVideo(id));
    }

    @Transactional(readOnly = true)
    public VideoFile file(Long id) {
        Video video = findVideo(id);
        return new VideoFile(storage.load(video.getStorageKey()),
                video.getContentType(), video.getSizeBytes());
    }

    @Transactional
    public void delete(Long id, Long ownerId) {
        Video video = findVideo(id);
        if (!video.getOwner().getId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can delete this video");
        }
        String key = video.getStorageKey();
        videos.delete(video);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                storage.deleteQuietly(key);
            }
        });
    }

    private Video findVideo(Long id) {
        return videos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found"));
    }

    public record VideoFile(Resource resource, String contentType, long sizeBytes) {
    }
}
