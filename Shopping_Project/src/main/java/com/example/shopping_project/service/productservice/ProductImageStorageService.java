package com.example.shopping_project.service.productservice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 관리자 상품 등록 화면에서 올린 이미지를 로컬 uploads 폴더에 저장한다.
 */
@Service
public class ProductImageStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final Path uploadRoot;

    public ProductImageStorageService(
            @Value("${app.upload.dir:uploads/products}") String uploadDir) throws IOException {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadRoot);
    }

    public Path getUploadRoot() {
        return uploadRoot;
    }

    /**
     * @return 웹에서 접근 가능한 경로 (/uploads/products/xxx.jpg). 파일이 없으면 null
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = file.getOriginalFilename();
        String extension = extensionOf(original);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다. (jpg, png, webp, gif)");
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path target = uploadRoot.resolve(filename);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("이미지 저장에 실패했습니다.", ex);
        }
        return "/uploads/products/" + filename;
    }

    private static String extensionOf(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
