// src/main/java/com/epr/entity/Services.java

package com.epr.entity;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "services",
        indexes = {
                @Index(name = "idx_services_public", columnList = "deleteStatus, displayStatus, postDate"),
                @Index(name = "idx_services_slug", columnList = "slug", unique = true),
                @Index(name = "idx_services_category", columnList = "category_id, deleteStatus"),
                @Index(name = "idx_services_subcategory", columnList = "subcategory_id"),
                @Index(name = "idx_services_home", columnList = "showHomeStatus, deleteStatus, displayStatus"),
                @Index(name = "idx_services_visited", columnList = "visited DESC")
        }
)
@Getter
@Setter
@DynamicInsert
@DynamicUpdate
public class Services {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, length = 100, nullable = false, updatable = false)
    private String uuid;

    @NotBlank
    @Column(length = 200, nullable = false)
    private String title;

    @NotBlank
    @Column(unique = true, length = 250, nullable = false)
    private String slug;

    @Column(length = 500)
    private String shortDescription;

    @Column(columnDefinition = "LONGTEXT")
    private String fullDescription;

    private String bannerImage;
    private String thumbnail;
    private String videoUrl;

    // SEO
    private String metaTitle;
    @Column(columnDefinition = "TEXT")
    private String metaKeyword;
    @Column(columnDefinition = "TEXT")
    private String metaDescription;

    // Status
    @Column(nullable = false, columnDefinition = "tinyint default 1")
    private int displayStatus = 1;

    @Column(nullable = false, columnDefinition = "tinyint default 2")
    private int showHomeStatus = 2;

    @Column(nullable = false, columnDefinition = "tinyint default 2")
    private int deleteStatus = 2;

    // Dates
    @CreationTimestamp
    @Column(name = "post_date", nullable = false, updatable = false)
    private LocalDateTime postDate;

    @UpdateTimestamp
    @Column(name = "modify_date")
    private LocalDateTime modifyDate;

    // Audit
    @Column(length = 100)
    private String addedByUUID;
    @Column(length = 100)
    private String modifyByUUID;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long visited = 0L;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    private Subcategory subcategory;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ServiceSection> sections = new ArrayList<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceFaq> faqs = new ArrayList<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceDocument> documents = new ArrayList<>();

    public void incrementVisit() {
        this.visited++;
    }
}