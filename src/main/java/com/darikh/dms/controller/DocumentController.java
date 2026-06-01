package com.darikh.dms.controller;

import com.darikh.dms.model.Category;
import com.darikh.dms.model.Document;
import com.darikh.dms.model.User;
import com.darikh.dms.repository.CategoryRepository;
import com.darikh.dms.repository.DocumentRepository;
import com.darikh.dms.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final Path uploadDir = Paths.get("uploads");

    public DocumentController(
            DocumentRepository documentRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository
    ) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @PostMapping("/upload")
    public Document uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long userId,
            @RequestParam String categoryName
    ) throws IOException {

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden"));

        Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(categoryName);
                    return categoryRepository.save(newCategory);
                });

        String storedFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadDir.resolve(storedFileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Document document = new Document();
        document.setFileName(file.getOriginalFilename());
        document.setFilePath(filePath.toString());
        document.setFileType(file.getContentType());
        document.setUploadDate(LocalDate.now());
        document.setVersion(1);
        document.setUser(user);
        document.setCategory(category);

        return documentRepository.save(document);
    }

    @GetMapping
    public List<Document> getDocuments() {
        return documentRepository.findAll();
    }

    @GetMapping("/search")
    public List<Document> searchDocuments(@RequestParam String keyword) {
        return documentRepository.findByFileNameContainingIgnoreCase(keyword);
    }

    @GetMapping("/filter")
    public List<Document> filterByCategory(@RequestParam String category) {
        return documentRepository.findByCategory_NameIgnoreCase(category);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) throws MalformedURLException {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dokument nicht gefunden"));

        Path path = Paths.get(document.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public void deleteDocument(@PathVariable Long id) throws IOException {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dokument nicht gefunden"));

        Path path = Paths.get(document.getFilePath());
        Files.deleteIfExists(path);

        documentRepository.deleteById(id);
    }
}