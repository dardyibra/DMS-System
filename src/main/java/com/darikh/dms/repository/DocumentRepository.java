package com.darikh.dms.repository;

import com.darikh.dms.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByFileNameContainingIgnoreCase(String keyword);

    List<Document> findByCategory_NameIgnoreCase(String categoryName);
}