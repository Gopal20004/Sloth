package com.demo.sloth.video;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class VideoStorage {

    private static final Logger LOG = LoggerFactory.getLogger(VideoStorage.class);
    private static final long MAX_BYTES = 100L * 1024 * 1024;

    private final Path root;

    public VideoStorage(@Value("${sloth.videos.path}") String directory) {
        root = Path.of(directory).toAbsolutePath().normalize();
    }

    public StoredFile save(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Video is empty");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.valueOf(413), "Video exceeds 100 MB");
        }

        String contentType = detectType(file);
        String extension = contentType.equals("video/mp4") ? ".mp4" : ".webm";
        String key = UUID.randomUUID() + extension;
        Path destination = resolve(key);
        try {
            Files.createDirectories(root);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, destination);
            }
            return new StoredFile(key, contentType, file.getSize());
        } catch (IOException exception) {
            deleteQuietly(key);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save video", exception);
        }
    }

    public Resource load(String key) {
        Path path = resolve(key);
        if (!Files.isRegularFile(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Video file not found");
        }
        return new FileSystemResource(path);
    }

    public void deleteQuietly(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException exception) {
            LOG.warn("Could not remove stored video {}", key, exception);
        }
    }

    private String detectType(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(12);
            if ("video/mp4".equals(file.getContentType()) && header.length >= 8
                    && "ftyp".equals(new String(header, 4, 4, StandardCharsets.US_ASCII))) {
                return "video/mp4";
            }
            if ("video/webm".equals(file.getContentType()) && header.length >= 4
                    && (header[0] & 0xff) == 0x1a && (header[1] & 0xff) == 0x45
                    && (header[2] & 0xff) == 0xdf && (header[3] & 0xff) == 0xa3) {
                return "video/webm";
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read video", exception);
        }
        throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only MP4 and WebM are supported");
    }

    private Path resolve(String key) {
        if (!key.matches("[0-9a-f-]{36}\\.(mp4|webm)")) {
            throw new IllegalArgumentException("Invalid video key");
        }
        return root.resolve(key).normalize();
    }

    public record StoredFile(String key, String contentType, long sizeBytes) {
    }
}
