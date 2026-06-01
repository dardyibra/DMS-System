package com.darikh.dms.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String filePath;

    private String fileType;

    private LocalDate uploadDate;

    private int version = 1;

    @ManyToOne
    private User user;

    @ManyToOne
    private Category category;
}