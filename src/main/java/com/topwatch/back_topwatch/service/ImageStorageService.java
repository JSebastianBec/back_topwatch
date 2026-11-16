package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.enums.ImageFolder;
import com.topwatch.back_topwatch.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final Map<String, String> ALLOWED =
            Map.of("image/jpeg", "jpg", "image/png", "png", "image/webp", "webp");

    private final S3Client s3;
    private final String bucket;
    private final String baseUrl;

    public ImageStorageService(S3Client s3,
                               @Value("${application.storage.s3.bucket}") String bucket,
                               @Value("${application.storage.s3.public-base-url}") String baseUrl) {
        this.s3 = s3;
        this.bucket = bucket;
        this.baseUrl = baseUrl;
    }

    public String upload(MultipartFile file, ImageFolder folder, Long userId) {
        String ext = ALLOWED.get(file.getContentType());
        if (file.isEmpty() || ext == null || !looksLikeImage(file)) {
            throw new BadRequestException("Only JPEG, PNG or WEBP images are allowed");
        }
        String key = "uploads/%s/%d/%s.%s".formatted(
                folder.name().toLowerCase(), userId, UUID.randomUUID(), ext);
        try (InputStream in = file.getInputStream()) {
            s3.putObject(PutObjectRequest.builder()
                            .bucket(bucket).key(key).contentType(file.getContentType()).build(),
                    RequestBody.fromInputStream(in, file.getSize()));
        } catch (IOException e) {
            throw new BadRequestException("Could not read the uploaded file");
        }
        return baseUrl + "/" + key;
    }

    /** Use before saving any image URL (uploaded or pasted). */
    public void validateImageUrl(String url, Long userId) {
        if (url == null || url.isBlank()) return;
        if (isOurs(url) && !isOwnedBy(url, userId)) {
            throw new BadRequestException("You can only use images you uploaded");
        }
    }

    public void deleteIfOwned(String url, Long userId) {
        if (!isOwnedBy(url, userId)) return; // external links and other users' files are never deleted
        s3.deleteObject(b -> b.bucket(bucket).key(keyOf(url)));
    }

    private boolean isOurs(String url) {
        return url != null && url.startsWith(baseUrl + "/uploads/");
    }

    private boolean isOwnedBy(String url, Long userId) {
        if (!isOurs(url)) return false;
        String[] parts = keyOf(url).split("/"); // uploads / folder / userId / file
        return parts.length == 4 && parts[2].equals(String.valueOf(userId));
    }

    private String keyOf(String url) {
        return url.substring(baseUrl.length() + 1);
    }

    // Content-Type comes from the client and can be faked, so check the real file header too
    private boolean looksLikeImage(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] h = in.readNBytes(12);
            if (h.length < 12) return false;
            boolean jpg  = (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF;
            boolean png  = (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G';
            boolean webp = h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                    && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P';
            return jpg || png || webp;
        } catch (IOException e) {
            return false;
        }
    }
}