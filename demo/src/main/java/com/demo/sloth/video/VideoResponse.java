package com.demo.sloth.video;

import java.time.Instant;

public record VideoResponse(Long id, Long ownerId, String ownerName, String gameSlug,
                            String title, String fileUrl, String contentType,
                            long sizeBytes, Instant createdAt) {

    static VideoResponse from(Video video) {
        return new VideoResponse(video.getId(), video.getOwner().getId(),
                video.getOwner().getDisplayName(),
                video.getGame() == null ? null : video.getGame().getSlug(),
                video.getTitle(), "/api/videos/" + video.getId() + "/file",
                video.getContentType(), video.getSizeBytes(), video.getCreatedAt());
    }
}
