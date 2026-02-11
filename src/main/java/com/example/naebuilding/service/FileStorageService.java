package com.example.naebuilding.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드 파일이 비어있습니다.");
        }

        String originalName = file.getOriginalFilename();
        String cleaned = StringUtils.cleanPath(originalName == null ? "" : originalName);
        String ext = getExtension(cleaned).toLowerCase();

        if (!StringUtils.hasText(ext)) {
            throw new IllegalArgumentException("파일 확장자가 없습니다.");
        }
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (jpg, jpeg, png, webp만 가능)");
        }

        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String savedName = UUID.randomUUID() + "." + ext;
            Path target = dir.resolve(savedName);

            // UUID라 충돌 거의 없지만, 의도대로 덮어쓰기 방지
            Files.copy(file.getInputStream(), target);

            return "/uploads/" + savedName;

        } catch (FileAlreadyExistsException e) {
            throw new RuntimeException("파일명이 충돌했습니다. 다시 시도해주세요.", e);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return (idx >= 0) ? filename.substring(idx + 1) : "";
    }

    public void deleteByUrl(String url) {
        if (url == null || url.isBlank()) return;

        // "/uploads/xxx.jpg" -> "xxx.jpg"
        String filename = url.startsWith("/uploads/") ? url.substring("/uploads/".length()) : url;

        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path target = dir.resolve(filename).normalize();

            // 보안: uploads 밖으로 못 나가게
            if (!target.startsWith(dir)) {
                throw new IllegalArgumentException("잘못된 파일 경로입니다.");
            }

            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 실패", e);
        }
    }

}
