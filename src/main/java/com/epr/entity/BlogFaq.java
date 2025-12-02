// src/main/java/com/epr/entity/BlogFaq.java
package com.epr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "blog_faqs")
@Getter
@Setter
public class BlogFaq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100, nullable = false)
    private String uuid;

    @NotBlank(message = "FAQ title is required")
    @Column(length = 500, nullable = false)
    private String title;

    @NotBlank(message = "FAQ description is required")
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "int default 1")
    private int displayStatus = 1; // 1 = show, 2 = hide

    private LocalDateTime postDate;

    private LocalDateTime modifyDate;

    @Column(length = 100)
    private String addedByUUID;

    @Column(length = 100)
    private String modifyByUUID;

    @Column(columnDefinition = "int default 2")
    private int deleteStatus = 2; // 1 = deleted, 2 = active

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blogs blog;

}