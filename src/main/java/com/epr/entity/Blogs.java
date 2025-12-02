// src/main/java/com/epr/entity/Blogs.java
package com.epr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blogs")
@Getter
@Setter
public class Blogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100, nullable = false)
    private String uuid;

    @NotBlank
    @Column(length = 255, nullable = false)
    private String title;

    @NotBlank
    @Column(unique = true, length = 300, nullable = false)
    private String slug;

    private String image;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    @NotBlank
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String description;

    @NotBlank
    @Column(length = 255, nullable = false)
    private String metaTitle;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String metaKeyword;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String metaDescription;

    @Column(columnDefinition = "int default 1")
    private Integer displayStatus = 1;

    @Column(nullable = false)
    private LocalDateTime postDate = LocalDateTime.now();

    private LocalDateTime modifyDate;

    @Column(length = 100)
    private String addedByUUID;

    @Column(length = 100)
    private String modifyByUUID;

    @Column(length = 100, nullable = false)
    private String postedByUuid;

    @Column(length = 100)
    private String postedByName;

    @Column(columnDefinition = "bigint default 0")
    private long visited = 0;

    @Column(columnDefinition = "int default 2")
    private int deleteStatus = 2;

    @Column(columnDefinition = "TEXT")
    private String searchKeyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    private Subcategory subcategory;

    @ManyToMany
    @JoinTable(
            name = "service_blogs",
            joinColumns = @JoinColumn(name = "blog_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<Services> services = new ArrayList<>();

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogFaq> faqs = new ArrayList<>();

}